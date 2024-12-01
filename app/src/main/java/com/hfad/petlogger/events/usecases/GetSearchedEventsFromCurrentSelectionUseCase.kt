package com.hfad.petlogger.events.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.events.EventDao
import com.hfad.petlogger.events.EventForList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class GetSearchedEventsFromCurrentSelectionUseCase(
    private val eventDao: EventDao,
    private val eventAmt: Int,
    private val currentSelection: LiveData<List<EventForList>>
): GetSearchedItemsUseCase<EventForList> {
    override var currentQuery: String = ""
    private var lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastEventId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    private val queryBuilder = BuildEventSearchQueryUseCase(eventAmt)

    override suspend fun invoke(): List<EventForList> = withContext(Dispatchers.IO) {
        currentSelection.value?.let {  currentEventSelection ->
            val builtQuery = queryBuilder(
                query = currentQuery,
                lastEventDate =  lastEventDate,
                lastEventId = lastEventId,
                eventIdSelectionPool = currentEventSelection.map{it.eventId}
            )
            builtQuery?.let { query ->
                val searchResultsFetched = async {
                    eventDao.searchEvents(query)
                }
                val currentSelectionMap = currentEventSelection.associateBy { it.eventId }
                val searchResults = searchResultsFetched.await()
                // update for further pagination
                lastEventId = searchResults.lastOrNull()?.eventId ?: Long.MAX_VALUE
                lastEventDate = searchResults.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
                _onLastPage = searchResults.size < eventAmt

                return@withContext searchResults.mapNotNull { currentSelectionMap[it.eventId] }
            }
        }
        listOf()
    }

    override fun resetCurrentPoint() {
        lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastEventId = Long.MAX_VALUE
        _onLastPage = false
    }
}