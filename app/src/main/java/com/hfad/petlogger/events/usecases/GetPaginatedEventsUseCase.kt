package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.events.EventForList
import java.time.OffsetDateTime

abstract class GetPaginatedEventsUseCase(private val eventAmt: Int): GetItemsUseCase<EventForList> {
    private var lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastEventId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<EventForList> {
        val events = fetchEvents(lastEventDate, lastEventId)
        lastEventId = events.lastOrNull()?.eventId ?: Long.MAX_VALUE
        lastEventDate = events.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = events.size < eventAmt
        return events.map{it.toEventForList()}
    }

    override fun resetCurrentPoint() {
        lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastEventId = Long.MAX_VALUE
        _onLastPage = false
    }

    abstract suspend fun fetchEvents(lastEventDate: OffsetDateTime, lastEventId: Long): List<Event>
}