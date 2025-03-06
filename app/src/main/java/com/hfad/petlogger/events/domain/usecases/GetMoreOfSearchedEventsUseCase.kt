package com.hfad.petlogger.events.domain.usecases

import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.events.data.EventDao
import com.hfad.petlogger.events.data.EventForList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMoreOfSearchedEventsUseCase(
    private val eventDao: EventDao,
    private val eventAmt: Int,
    pickFrom: BuildEventSearchQueryUseCase.Pick? = null
): GetSearchedItemsUseCase<EventForList> {
    override var currentQuery: String = ""
    private var lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastEventId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    private val queryBuilder = BuildEventSearchQueryUseCase(eventAmt = eventAmt, pickFrom =  pickFrom)
    override suspend fun invoke(): List<EventForList> = withContext(Dispatchers.IO) {
        val builtQuery = queryBuilder(currentQuery, lastEventDate, lastEventId)
        builtQuery?.let { query ->
            val searchResults = eventDao.searchEvents(query)

            // update for further pagination
            lastEventId = searchResults.lastOrNull()?.eventId ?: Long.MAX_VALUE
            lastEventDate = searchResults.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
            _onLastPage = searchResults.size < eventAmt

            return@withContext searchResults.map{it.toEventForList()}
        }
        listOf()
    }

    override fun resetCurrentPoint() {
        lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastEventId = Long.MAX_VALUE
        _onLastPage = false
    }
}