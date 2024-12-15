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
    private val currentSelection: LiveData<List<EventForList>>
): GetSearchedItemsUseCase<EventForList> {
    override var currentQuery: String = ""
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    private val queryBuilder = BuildEventSearchQueryUseCase(get = BuildEventSearchQueryUseCase.Get.OnlyIds)
    private var idsAlreadyFetched: Boolean = false
    private var fetchedIds: List<Long> = listOf()

    override suspend fun invoke(): List<EventForList> = withContext(Dispatchers.IO) {
        currentSelection.value?.let {  currentEventSelection ->
            val currentSelectionMap = currentEventSelection.associateBy { it.eventId }
            if (idsAlreadyFetched) {
                return@withContext fetchedIds.mapNotNull { currentSelectionMap[it] }
            }
            else {
                val builtQuery = queryBuilder(
                    query = currentQuery
                )
                builtQuery?.let { query ->
                    val searchResultsFetched = async {
                        eventDao.searchEventIds(query)
                    }
                    val searchResults = searchResultsFetched.await()
                    fetchedIds = searchResults
                    idsAlreadyFetched = true
                    return@withContext searchResults.mapNotNull { currentSelectionMap[it] }
                }
            }
        }
        listOf()
    }

    // will only get called if query changes
    override fun resetCurrentPoint() {
        _onLastPage = false
        idsAlreadyFetched = false
        fetchedIds = listOf()
    }
}