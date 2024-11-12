package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.EventDao
import com.hfad.petlogger.events.EventForList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSingleEventUseCase(private val eventDao: EventDao, private val eventId: Long): GetItemsUseCase<EventForList> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<EventForList> = withContext(Dispatchers.IO) {
        listOf(eventDao.get(eventId).toEventForList())
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}