package com.hfad.petlogger.notes.usecases

import android.util.Log
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
        val searchedPets = parsedSearch["pet"]?.toHashSet()?.toList()?:listOf()
        val searchedTags = parsedSearch["#"]?.toHashSet()?.toList()?:listOf()

        val queryBuilder = StringBuilder()
        val queryParams = mutableListOf<Any>()

        // FTS4 matches
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("WITH matched_notes AS ( SELECT note_id FROM note_table_fts WHERE note_table_fts MATCH ? ) ")
            queryParams.add(nonCategorizedSearch.joinToString(separator=" "))
        }

        if (pickFrom!=null) {
            if (queryBuilder.isNotEmpty()) {
                queryBuilder.append(", ")
            } else if (queryBuilder.isEmpty()) {
                queryBuilder.append("WITH ")
            }
        }

        when (pickFrom) {
            is Pick.FromEvent -> {
                queryBuilder.append("notes_of_given_event AS (SELECT note_id FROM event_note_table WHERE event_id = ?) ")
                queryParams.add(pickFrom.eventId)
            }
            is Pick.FromPet -> {
                queryBuilder.append("notes_of_given_pet AS (SELECT note_id FROM pet_note_table WHERE pet_id = ?) ")
                queryParams.add(pickFrom.petId)
            }
            is Pick.FromPhoto -> {
                queryBuilder.append("notes_of_given_photo AS (SELECT note_id FROM photo_note_table WHERE photo_id = ?) ")
                queryParams.add(pickFrom.photoId)
            }
            is Pick.FromWeight -> {
                queryBuilder.append("notes_of_given_weight AS (SELECT note_id FROM weight_note_table WHERE weight_id = ?) ")
                queryParams.add(pickFrom.weightId)
            }
            is Pick.FromTag -> {
                queryBuilder.append("notes_of_given_tag AS (SELECT note_id FROM note_tag_table WHERE tag_id = ?) ")
                queryParams.add(pickFrom.tagId)
            }
            null -> {}
        }

        // Base query to join Note with associations
        queryBuilder.append("SELECT note_table.* FROM note_table ")
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("JOIN matched_notes ON note_table.note_id=matched_notes.note_id ")
        }
        pickFrom?.let {
            when (pickFrom) {
                is Pick.FromEvent -> {
                    queryBuilder.append("JOIN notes_of_given_event ON note_table.note_id=notes_of_given_event.note_id ")
                }
                is Pick.FromPet -> {
                    queryBuilder.append("JOIN notes_of_given_pet ON note_table.note_id=notes_of_given_pet.note_id ")
                }
                is Pick.FromPhoto -> {
                    queryBuilder.append("JOIN notes_of_given_photo ON note_table.note_id=notes_of_given_photo.note_id ")
                }
                is Pick.FromWeight -> {
                    queryBuilder.append("JOIN notes_of_given_weight ON note_table.note_id=notes_of_given_weight.note_id ")
                }
                is Pick.FromTag -> {
                    queryBuilder.append("JOIN notes_of_given_tag ON note_table.note_id=notes_of_given_tag.note_id ")
                }
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

        Log.d("NoteSearchQuery", "Query: $queryBuilder")
        Log.d("NoteSearchQuery", "Params: ${queryParams.map{it.toString()}}")

        return SimpleSQLiteQuery(queryBuilder.toString(), queryParams.toTypedArray())
    }

    sealed class Pick {
        data class FromPet(val petId: Long): Pick()
        data class FromEvent(val eventId: Long): Pick()
        data class FromWeight(val weightId: Long): Pick()
        data class FromPhoto(val photoId: Long): Pick()
        data class FromTag(val tagId: Long): Pick()
    }
}