package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.events.EventForList
import java.time.OffsetDateTime

class GetMoreEventsOfPhotoUseCase(
    private val mediaRepository: MediaRepository,
    private val photoId: Long,
    private val eventAmt: Int
): GetPaginatedEventsUseCase(eventAmt) {
    override suspend fun fetchEvents(lastEventDate: OffsetDateTime, lastEventId: Long): List<Event>
        = mediaRepository.getEventsOfPhotoPaginated(photoId, lastEventDate, lastEventId, eventAmt)
}