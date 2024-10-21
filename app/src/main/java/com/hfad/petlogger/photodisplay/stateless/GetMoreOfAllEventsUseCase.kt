package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventForList
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.util.Constants

class GetMoreOfAllEventsUseCase(
    private val eventRepository: EventRepository,
    private val eventAmt: Int
): GetItemsUseCase<EventForList> {
    private var lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastEventId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<EventForList> {
        val events = eventRepository.getAllEventsPaginated(lastEventDate, lastEventId, eventAmt)
        lastEventId = events.lastOrNull()?.eventId ?: Long.MAX_VALUE
        lastEventDate = events.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = events.size < eventAmt
        return events.map { it.toEventForList() }
    }

    override fun resetCurrentPoint() {
        lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastEventId = Long.MAX_VALUE
        _onLastPage = false
    }
}