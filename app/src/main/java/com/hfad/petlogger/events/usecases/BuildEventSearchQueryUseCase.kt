package com.hfad.petlogger.events.usecases

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.search.GetBoundingSearchDatesUseCase
import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.util.Converter
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.tags.Tag
import java.time.OffsetDateTime

class BuildEventSearchQueryUseCase(
    private val get: Get = Get.Event,
    private val eventAmt: Int? = null,
    private val pickFrom: Pick? = null
) {
    private val parseSearchQuery = ParseSearchQueryUseCase(listOf("pet", "before", "after"))
    operator fun invoke(
        query: String,
        lastEventDate: OffsetDateTime = Constants.OFFSET_DATE_TIME_MAX_ALLOWED,
        lastEventId: Long = Long.MAX_VALUE
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
            queryBuilder.append("WITH matched_events AS ( SELECT event_id FROM event_table_fts WHERE event_table_fts MATCH ? ) ")
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
            is Pick.FromNote -> {
                queryBuilder.append("events_of_given_note AS (SELECT event_id FROM event_note_table WHERE note_id = ?) ")
                queryParams.add(pickFrom.noteId)
            }
            is Pick.FromPet -> {
                queryBuilder.append("events_of_given_pet AS (SELECT event_id FROM event_pet_table WHERE pet_id = ?) ")
                queryParams.add(pickFrom.petId)
            }
            is Pick.FromPhoto -> {
                queryBuilder.append("events_of_given_photo AS (SELECT event_id FROM photo_event_table WHERE photo_id = ?) ")
                queryParams.add(pickFrom.photoId)
            }
            is Pick.FromTag -> {
                queryBuilder.append("events_of_given_tag AS (SELECT event_id FROM event_tag_table WHERE tag_id = ?) ")
                queryParams.add(pickFrom.tagId)
            }
            null -> {}
        }

        // Base query to join Event with associations
        when(get) {
            Get.Event -> {
                queryBuilder.append("SELECT event_table.* ")
            }
            Get.OnlyIds -> {
                queryBuilder.append("SELECT event_table.event_id ")
            }
        }
        queryBuilder.append("FROM event_table ")
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("JOIN matched_events ON event_table.event_id=matched_events.event_id ")
        }
        when (pickFrom) {
            is Pick.FromNote -> {
                queryBuilder.append("JOIN events_of_given_note ON event_table.event_id=events_of_given_note.event_id ")
            }
            is Pick.FromPet -> {
                queryBuilder.append("JOIN events_of_given_pet ON event_table.event_id=events_of_given_pet.event_id ")
            }
            is Pick.FromPhoto -> {
                queryBuilder.append("JOIN events_of_given_photo ON event_table.event_id=events_of_given_photo.event_id ")
            }
            is Pick.FromTag -> {
                queryBuilder.append("JOIN events_of_given_tag ON event_table.event_id=events_of_given_tag.event_id ")
            }
            null -> {}
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

        queryBuilder.append("ORDER BY datetime(event_table.event_date) DESC, event_table.event_id DESC ")
        eventAmt?.let {
            queryBuilder.append("LIMIT ? ")
            queryParams.add(eventAmt)
        }

        Log.d("EventSearch", "Query: $queryBuilder")
        Log.d("EventSearch", "Params: ${queryParams.map{it.toString()}}")

        return SimpleSQLiteQuery(queryBuilder.toString(), queryParams.toTypedArray())
    }

    sealed class Pick {
        data class FromPet(val petId: Long): Pick()
        data class FromNote(val noteId: Long): Pick()
        data class FromPhoto(val photoId: Long): Pick()
        data class FromTag(val tagId: Long): Pick()
    }

    sealed class Get {
        data object OnlyIds: Get()
        data object Event: Get()
    }
}