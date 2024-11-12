package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.EventDao
import com.hfad.petlogger.events.EventForList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAllEventsUseCase(private val eventDao: EventDao): GetItemsUseCase<EventForList> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<EventForList> = withContext(Dispatchers.IO) {
        eventDao.getAll().map{it.toEventForList()}
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}