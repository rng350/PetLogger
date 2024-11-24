package com.hfad.petlogger.events.usecases

import android.util.Log
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

class GetMoreOfSearchedEventsFromAllUseCase(
    private val eventDao: EventDao,
    private val eventAmt: Int
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

        // Base query to join Event with associations
        queryBuilder.append("SELECT event_table.* FROM event_table JOIN event_table_fts ON event_table.event_id=event_table_fts.event_id ")
        if (searchedPets.isNotEmpty()) {
            queryBuilder.append("""
                JOIN event_pet_table ON event_table.event_id = event_pet_table.event_id 
                JOIN pet_table ON pet_table.pet_id = event_pet_table.pet_id 
            """)
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("""
                JOIN event_tag_table ON event_table.event_id = event_tag_table.event_id 
                JOIN tag_table ON tag_table.tag_id = event_tag_table.tag_id 
            """)
        }
        //queryBuilder.append("WHERE 1 = 1 ") // just so we can have a "WHERE" clause regardless
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
            queryBuilder.append("AND pet_table.pet_name IN ${searchedPets.joinToString(prefix="(", separator=",", postfix=")"){"?"}} ")
            queryParams.addAll(searchedPets)
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("AND tag_table.tag_name IN ${searchedTags.joinToString(prefix="(", separator=",", postfix=")"){"?"}} ")
            queryParams.addAll(searchedTags)
        }
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("AND event_table_fts MATCH ? ")
            queryParams.add(nonCategorizedSearch.joinToString(separator=" "))
        }
        queryBuilder.append("GROUP BY event_table.event_id ")

        val havingCountQuery = StringBuilder()
        if (searchedPets.isNotEmpty()) {
            havingCountQuery.append("HAVING COUNT(DISTINCT pet_table.pet_name) = ? ")
            queryParams.add(searchedPets.size)
        }
        if (searchedTags.isNotEmpty()) {
            havingCountQuery.append("${if (havingCountQuery.isNotEmpty()) "AND" else "HAVING"} COUNT(DISTINCT tag_table.tag_name) = ? ")
            queryParams.add(searchedTags.size)
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
}