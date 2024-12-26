package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.events.Event
import com.hfad.petlogger.events.EventDao
import java.time.OffsetDateTime

class GetMoreEventsOfTagUseCase(
    private val eventDao: EventDao,
    private val tagId: Long,
    private val eventsAmt: Int
): GetPaginatedEventsUseCase(eventsAmt) {
    override suspend fun fetchEvents(lastEventDate: OffsetDateTime, lastEventId: Long): List<Event>
        = eventDao.getEventsOfTagPaginated(tagId, lastEventDate, lastEventId, eventsAmt)
}