package com.hfad.petlogger.notes.usecases

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.search.GetBoundingSearchDatesUseCase
import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.common.util.Converter
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.tags.Tag
import java.time.OffsetDateTime

class BuildNoteSearchQueryUseCase(
    private val notesAmt: Int,
    private val pickFrom: Pick? = null
) {
    private val parseSearchQuery = ParseSearchQueryUseCase(listOf("pet", "before", "after"))
    operator fun invoke(
        query: String,
        lastNoteEditedDate: OffsetDateTime,
        lastNoteId: Long,
        noteIdSelectionPool: List<Long>? = null
    ): SimpleSQLiteQuery? {
        val parsedSearch = parseSearchQuery(query)
        // get bounding dates
        val getBoundingSearchDates = GetBoundingSearchDatesUseCase()
        val startAndEndDates = getBoundingSearchDates(parsedSearch["before"]?:listOf(), parsedSearch["after"]?:listOf())
        if (startAndEndDates == GetBoundingSearchDatesUseCase.Result.Invalid) return null

        val nonCategorizedSearch = parsedSearch[null]?:listOf()
        val searchedPets =
            if (pickFrom is Pick.FromPet && pickFrom.petName.isNotEmpty()) {
                val petsHashedSet = parsedSearch["pet"]?.toHashSet() ?: HashSet<String>()
                petsHashedSet.add(pickFrom.petName)
                petsHashedSet.toList()
            } else parsedSearch["pet"]?.toHashSet()?.toList()?:listOf()
        val searchedTags =
            if (pickFrom is Pick.FromTag && pickFrom.tagName.isNotEmpty()) {
                val tagsHashedSet = parsedSearch["#"]?.toHashSet() ?: HashSet<String>()
                tagsHashedSet.add(pickFrom.tagName)
                tagsHashedSet.toList()
            } else parsedSearch["#"]?.toHashSet()?.toList()?:listOf()

        val queryBuilder = StringBuilder()
        val queryParams = mutableListOf<Any>()

        // FTS4 matches
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("WITH matched_notes AS ( SELECT note_id FROM note_table_fts WHERE note_table_fts MATCH ? ) ")
            queryParams.add(nonCategorizedSearch.joinToString(separator=" "))
        }
        // Base query to join Note with associations
        queryBuilder.append("SELECT note_table.* FROM note_table ")
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("JOIN matched_notes ON note_table.note_id=matched_notes.note_id ")
        }
        pickFrom?.let {
            when (pickFrom) {
                is Pick.FromPhoto -> {
                    queryBuilder.append("JOIN photo_note_table ON note_table.note_id=photo_note_table.note_id ")
                }
                is Pick.FromEvent -> {
                    queryBuilder.append("JOIN event_note_table ON note_table.note_id=event_note_table.note_id ")
                }
                is Pick.FromWeight -> {
                    queryBuilder.append("JOIN weight_note_table ON note_table.note_id=weight_note_table.note_id ")
                }
                else -> {}
            }
        }
        if (searchedPets.isNotEmpty()) {
            queryBuilder.append("JOIN pet_note_table ON note_table.note_id=pet_note_table.note_id ")
            queryBuilder.append("JOIN pet_table ON pet_table.pet_id=pet_note_table.pet_id ")
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("JOIN note_tag_table ON note_table.note_id=note_tag_table.note_id ")
            queryBuilder.append("JOIN tag_table ON tag_table.tag_id=note_tag_table.tag_id ")
        }

        // for pagination
        queryBuilder.append("WHERE (datetime(note_table.note_last_updated), note_table.note_id) < (datetime(?), ?) ")
        queryParams.add("${Converter.fromOffsetDateTime(lastNoteEditedDate)}")
        queryParams.add(lastNoteId)

        when (startAndEndDates) {
            is GetBoundingSearchDatesUseCase.Result.BoundingEndSearchDate -> {
                queryBuilder.append("AND datetime(note_table.note_last_updated) < datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingSearchDates -> {
                queryBuilder.append("AND datetime(note_table.note_last_updated) > datetime(?) ")
                queryBuilder.append("AND datetime(note_table.note_last_updated) < datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.startDate)}")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingStartSearchDate -> {
                queryBuilder.append("AND datetime(note_table.note_last_updated) > datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.startDate)}")
            }
            else -> {}
        }

        if (searchedPets.isNotEmpty()) {
            queryBuilder.append("AND pet_table.pet_name IN ${searchedPets.joinToString(prefix="(", separator=",", postfix=")"){"?"}} ")
            queryParams.addAll(searchedPets)
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("AND tag_table.tag_name IN ${searchedTags.joinToString(prefix="(", separator=",", postfix=")"){"?"}} ")
            queryParams.addAll(searchedTags)
        }
        if (pickFrom is Pick.FromPhoto) {
            queryBuilder.append("AND photo_note_table.photo_id = ? ")
            queryParams.add(pickFrom.photoId)
        }
        else if (pickFrom is Pick.FromWeight) {
            queryBuilder.append("AND weight_note_table.weight_id = ? ")
            queryParams.add(pickFrom.weightId)
        }

        noteIdSelectionPool?.let {
            if (noteIdSelectionPool.isEmpty()) {
                return null
            }
            queryBuilder.append("AND note_table.note_id IN ${noteIdSelectionPool.joinToString(prefix="(",separator=",",postfix=")"){"?"}} ")
            queryParams.addAll(noteIdSelectionPool)
        }

        queryBuilder.append("GROUP BY note_table.note_id ")

        val havingCountQuery = StringBuilder()
        if (searchedPets.isNotEmpty()) {
            havingCountQuery.append("HAVING COUNT(DISTINCT pet_table.pet_name) = ? ")
            queryParams.add(searchedPets.size)
        }
        if (searchedTags.isNotEmpty()) {
            havingCountQuery.append("${if (havingCountQuery.isNotEmpty()) "AND" else "HAVING"} COUNT(DISTINCT note_tag_table.tag_id) = ? ")
            queryParams.add(searchedTags.size)
        }
        queryBuilder.append(havingCountQuery)

        queryBuilder.append("ORDER BY datetime(note_table.note_last_updated) DESC, note_table.note_id DESC LIMIT ? ")
        queryParams.add(notesAmt)

        return SimpleSQLiteQuery(queryBuilder.toString(), queryParams.toTypedArray())
    }

    sealed class Pick {
        data class FromPet(private val pet: LiveData<Pet>): Pick() {
            val petName: String get() = pet.value?.petName ?: ""
        }
        data class FromEvent(val eventId: Long): Pick()
        data class FromWeight(val weightId: Long): Pick()
        data class FromPhoto(val photoId: Long): Pick()
        data class FromTag(private val tag: LiveData<Tag>): Pick() {
            val tagName: String get() = tag.value?.tagName ?: ""
        }
    }
}