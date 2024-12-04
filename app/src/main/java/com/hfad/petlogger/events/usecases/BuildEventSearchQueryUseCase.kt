package com.hfad.petlogger.events.usecases

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.search.GetBoundingSearchDatesUseCase
import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.common.util.Converter
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.tags.Tag
import java.time.OffsetDateTime

class BuildEventSearchQueryUseCase(
    private val eventAmt: Int,
    private val pickFrom: Pick? = null
) {
    private val parseSearchQuery = ParseSearchQueryUseCase(listOf("pet", "before", "after"))
    operator fun invoke(
        query: String,
        lastEventDate: OffsetDateTime,
        lastEventId: Long,
        eventIdSelectionPool: List<Long>? = null
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
            queryBuilder.append("WITH matched_events AS ( SELECT event_id FROM event_table_fts WHERE event_table_fts MATCH ? ) ")
            queryParams.add(nonCategorizedSearch.joinToString(separator=" "))
        }
        // Base query to join Event with associations
        queryBuilder.append("SELECT event_table.* FROM event_table ")
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("JOIN matched_events ON event_table.event_id=matched_events.event_id ")
        }
        pickFrom?.let {
            when (pickFrom) {
                is Pick.FromNote -> {
                    queryBuilder.append("JOIN event_note_table ON event_table.event_id=event_note_table.event_id ")
                }
                is Pick.FromPhoto -> {
                    queryBuilder.append("JOIN photo_event_table ON event_table.event_id=photo_event_table.event_id ")
                }
                else -> {}
            }
        }

        if (searchedPets.isNotEmpty()) {
            queryBuilder.append("JOIN event_pet_table ON event_table.event_id = event_pet_table.event_id ")
            queryBuilder.append("JOIN pet_table ON pet_table.pet_id = event_pet_table.pet_id ")
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("JOIN event_tag_table ON event_table.event_id = event_tag_table.event_id ")
            queryBuilder.append("JOIN tag_table ON tag_table.tag_id = event_tag_table.tag_id ")
        }

        queryBuilder.append("WHERE (datetime(event_table.event_date), event_table.event_id) < (datetime(?), ?) ")
        queryParams.add("${Converter.fromOffsetDateTime(lastEventDate)}")
        queryParams.add(lastEventId)

        when (startAndEndDates) {
            is GetBoundingSearchDatesUseCase.Result.BoundingEndSearchDate -> {
                queryBuilder.append("AND datetime(event_table.event_date) < datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingSearchDates -> {
                queryBuilder.append("AND datetime(event_table.event_date) > datetime(?) ")
                queryBuilder.append("AND datetime(event_table.event_date) < datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.startDate)}")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingStartSearchDate -> {
                queryBuilder.append("AND datetime(event_table.event_date) > datetime(?) ")
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
            queryBuilder.append("AND photo_event_table.photo_id = ? ")
            queryParams.add(pickFrom.photoId)
        }
        else if (pickFrom is Pick.FromNote) {
            queryBuilder.append("AND event_note_table.note_id = ? ")
            queryParams.add(pickFrom.noteId)
        }
        eventIdSelectionPool?.let {
            if (eventIdSelectionPool.isEmpty()) {
                return null
            }
            queryBuilder.append("AND event_table.event_id IN ${eventIdSelectionPool.joinToString(prefix="(",separator=",",postfix=")"){"?"}} ")
            queryParams.addAll(eventIdSelectionPool)
        }
        queryBuilder.append("GROUP BY event_table.event_id ")

        val havingCountQuery = StringBuilder()
        if (searchedPets.isNotEmpty()) {
            havingCountQuery.append("HAVING COUNT(DISTINCT pet_table.pet_name) = ? ")
            queryParams.add(searchedPets.size)
        }
        if (searchedTags.isNotEmpty()) {
            havingCountQuery.append("${if (havingCountQuery.isNotEmpty()) "AND" else "HAVING"} COUNT(DISTINCT event_tag_table.tag_id) = ? ")
            queryParams.add(searchedTags.size)
        }
        queryBuilder.append(havingCountQuery)

        queryBuilder.append("ORDER BY datetime(event_table.event_date) DESC, event_table.event_id DESC LIMIT ? ")
        queryParams.add(eventAmt)

        return SimpleSQLiteQuery(queryBuilder.toString(), queryParams.toTypedArray())
    }

    sealed class Pick {
        data class FromPet(private val pet: LiveData<Pet>): Pick() {
            val petName: String get() = pet.value?.petName ?: ""
        }
        data class FromNote(val noteId: Long): Pick()
        data class FromPhoto(val photoId: Long): Pick()
        data class FromTag(private val tag: LiveData<Tag>): Pick() {
            val tagName: String get() = tag.value?.tagName ?: ""
        }
    }
}