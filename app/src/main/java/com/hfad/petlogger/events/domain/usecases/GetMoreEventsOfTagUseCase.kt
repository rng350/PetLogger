package com.hfad.petlogger.events.domain.usecases

import com.hfad.petlogger.events.data.Event
import com.hfad.petlogger.events.data.EventDao
import java.time.OffsetDateTime

class GetMoreEventsOfTagUseCase(
    private val eventDao: EventDao,
    private val tagId: Long,
    private val eventsAmt: Int
): GetPaginatedEventsUseCase(eventsAmt) {
    override suspend fun fetchEvents(lastEventDate: OffsetDateTime, lastEventId: Long): List<Event>
        = eventDao.getEventsOfTagPaginated(tagId, lastEventDate, lastEventId, eventsAmt)
}