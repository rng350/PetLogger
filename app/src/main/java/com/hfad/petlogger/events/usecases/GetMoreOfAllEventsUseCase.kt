package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.events.EventForList
import com.hfad.petlogger.events.EventRepository
import java.time.OffsetDateTime

class GetMoreOfAllEventsUseCase(
    private val eventRepository: EventRepository,
    private val eventAmt: Int
): GetPaginatedEventsUseCase(eventAmt) {
    override suspend fun fetchEvents(
        lastEventDate: OffsetDateTime,
        lastEventId: Long
    ): List<Event>
        = eventRepository.getAllEventsPaginated(lastEventDate, lastEventId, eventAmt)
}