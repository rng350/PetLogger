package com.hfad.petlogger.events.domain.usecases

import com.hfad.petlogger.events.data.Event
import com.hfad.petlogger.photos.domain.MediaRepository
import java.time.OffsetDateTime

class GetMoreEventsOfPhotoUseCase(
    private val mediaRepository: MediaRepository,
    private val photoId: Long,
    private val eventAmt: Int
): GetPaginatedEventsUseCase(eventAmt) {
    override suspend fun fetchEvents(lastEventDate: OffsetDateTime, lastEventId: Long): List<Event>
        = mediaRepository.getEventsOfPhotoPaginated(photoId, lastEventDate, lastEventId, eventAmt)
}