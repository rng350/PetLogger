package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.util.Constants
import java.time.OffsetDateTime

class GetMoreEventsOfPhotosUseCase(
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
        val events = mediaRepository.getPhotoEventsAsListPaginated(photoId, lastEventDate, lastEventId, eventAmt)
        lastEventId = events.lastOrNull()?.eventId ?: Long.MAX_VALUE
        lastEventDate = events.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = events.size < eventAmt
        return events
    }
}