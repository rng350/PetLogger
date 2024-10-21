package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.util.Constants

class GetMoreEventsOfPhotoUseCase(
    private val mediaRepository: MediaRepository,
    private val photoId: Long,
    private val eventAmt: Int
): GetItemsUseCase<Event> {
    private var lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastEventId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Event> {
        val events = mediaRepository.getEventsOfPhotoPaginated(photoId, lastEventDate, lastEventId, eventAmt)
        lastEventId = events.lastOrNull()?.eventId ?: Long.MAX_VALUE
        lastEventDate = events.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = events.size < eventAmt
        return events
    }

    override fun resetCurrentPoint() {
        lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastEventId = Long.MAX_VALUE
        _onLastPage = false
    }
}