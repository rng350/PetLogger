package com.hfad.petlogger.events.usecases

import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.search.GetBoundingSearchDatesUseCase
import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.util.Converter
import com.hfad.petlogger.events.EventDao
import com.hfad.petlogger.events.EventForList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMoreOfSearchedEventsUseCase(
    private val eventDao: EventDao,
    private val eventAmt: Int,
    private val pickFrom: PickFrom? = null
): GetSearchedItemsUseCase<EventForList> {
    override var currentQuery: String = ""
    private var lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastEventId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    private val parseSearchQuery = ParseSearchQueryUseCase(listOf("pet", "before", "after"))
    override suspend fun invoke(): List<EventForList> = withContext(Dispatchers.IO) {
        val parsedSearch = parseSearchQuery(currentQuery)
        // get bounding dates
        val getBoundingSearchDates = GetBoundingSearchDatesUseCase()
        val startAndEndDates = getBoundingSearchDates(parsedSearch["before"]?:listOf(), parsedSearch["after"]?:listOf())
        if (startAndEndDates == GetBoundingSearchDatesUseCase.Result.Invalid) return@withContext listOf()

        val nonCategorizedSearch = parsedSearch[null]?:listOf()
        val searchedPets = parsedSearch["pet"]?:listOf()
        val searchedTags = parsedSearch["#"]?:listOf()

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
                is PickFrom.Note -> {
                    queryBuilder.append("JOIN event_note_table ON event_table.event_id=event_note_table.event_id ")
                }
                is PickFrom.Pet -> {
                    queryBuilder.append("JOIN event_pet_table ON event_table.event_id=event_pet_table.event_id ")
                }
                is PickFrom.Photo -> {
                    queryBuilder.append("JOIN photo_event_table ON event_table.event_id=photo_event_table.event_id ")
                }
                is PickFrom.Tag -> {
                    queryBuilder.append("JOIN event_tag_table ON event_table.event_id=event_tag_table.event_id ")
                }
            }
        }

        if (searchedPets.isNotEmpty()) {
            if (pickFrom !is PickFrom.Pet) {
                queryBuilder.append("JOIN event_pet_table ON event_table.event_id = event_pet_table.event_id ")
            }
            queryBuilder.append("JOIN pet_table ON pet_table.pet_id = event_pet_table.pet_id ")
        }
        if (searchedTags.isNotEmpty()) {
            if (pickFrom !is PickFrom.Tag) {
                queryBuilder.append("JOIN event_tag_table ON event_table.event_id = event_tag_table.event_id ")
            }
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
                queryBuilder.append("AND datetime(event_table.event_date) BETWEEN datetime(?) AND datetime(?) ")
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
            queryBuilder.append("AND ${if (pickFrom is PickFrom.Pet) "(" else ""}pet_table.pet_name IN ${searchedPets.joinToString(prefix="(", separator=",", postfix=")"){"?"}} ")
            queryParams.addAll(searchedPets)
        }
        if (pickFrom is PickFrom.Pet) {
            queryBuilder.append("${if (searchedPets.isNotEmpty()) "OR" else "AND" } event_pet_table.pet_id = ?${if (searchedPets.isNotEmpty()) ")" else "" } ")
            queryParams.add(pickFrom.petId)
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("AND ${if (pickFrom is PickFrom.Tag) "(" else ""}tag_table.tag_name IN ${searchedTags.joinToString(prefix="(", separator=",", postfix=")"){"?"}} ")
            queryParams.addAll(searchedTags)
        }
        if (pickFrom is PickFrom.Tag) {
            queryBuilder.append("${if (searchedTags.isNotEmpty()) "OR" else "AND" } event_tag_table.tag_id = ? ${if (searchedTags.isNotEmpty()) ")" else ""} ")
            queryParams.add(pickFrom.tagId)
        }
        if (pickFrom is PickFrom.Photo) {
            queryBuilder.append("AND photo_event_table.photo_id = ? ")
            queryParams.add(pickFrom.photoId)
        }
        if (pickFrom is PickFrom.Note) {
            queryBuilder.append("AND event_note_table.note_id = ? ")
            queryParams.add(pickFrom.noteId)
        }
        queryBuilder.append("GROUP BY event_table.event_id ")

        val havingCountQuery = StringBuilder()
        if (searchedPets.isNotEmpty() || pickFrom is PickFrom.Pet) {
            havingCountQuery.append("HAVING COUNT(DISTINCT event_pet_table.pet_id) = ? ")
            queryParams.add(searchedPets.size + if (pickFrom is PickFrom.Pet) 1 else 0)
        }
        if (searchedTags.isNotEmpty() || pickFrom is PickFrom.Tag) {
            havingCountQuery.append("${if (havingCountQuery.isNotEmpty()) "AND" else "HAVING"} COUNT(DISTINCT event_tag_table.tag_id) = ? ")
            queryParams.add(searchedTags.size + if (pickFrom is PickFrom.Tag) 1 else 0)
        }
        queryBuilder.append(havingCountQuery)

        queryBuilder.append("ORDER BY datetime(event_table.event_date) DESC, event_table.event_id DESC LIMIT $eventAmt")

        val searchResults = eventDao.searchEvents(SimpleSQLiteQuery(queryBuilder.toString(), queryParams.toTypedArray()))

        // update for further pagination
        lastEventId = searchResults.lastOrNull()?.eventId ?: Long.MAX_VALUE
        lastEventDate = searchResults.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = searchResults.size < eventAmt

        searchResults.map{it.toEventForList()}
    }

    override fun resetCurrentPoint() {
        lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastEventId = Long.MAX_VALUE
        _onLastPage = false
    }

    sealed class PickFrom {
        data class Pet(val petId: Long): PickFrom()
        data class Note(val noteId: Long): PickFrom()
        data class Photo(val photoId: Long): PickFrom()
        data class Tag(val tagId: Long): PickFrom()
    }
}