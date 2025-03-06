package com.hfad.petlogger.events.domain.usecases

import com.hfad.petlogger.events.data.Event
import com.hfad.petlogger.events.domain.EventRepository
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