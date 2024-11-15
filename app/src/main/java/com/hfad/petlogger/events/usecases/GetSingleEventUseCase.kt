package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.events.EventDao
import com.hfad.petlogger.events.EventForList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSingleEventUseCase(private val eventDao: EventDao, private val eventId: Long): GetSingleItemUseCase<EventForList> {

    override suspend fun invoke(): EventForList = withContext(Dispatchers.IO) {
        eventDao.get(eventId).toEventForList()
    }
}