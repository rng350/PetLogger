package com.hfad.petlogger.photos.domain.usecases

import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.photos.data.Photo
import java.time.OffsetDateTime

class GetMorePhotosOfEventUseCase(
    private val eventRepository: EventRepository,
    private val eventId: Long,
    private val photosAmt: Int
): GetPaginatedPhotosUseCase(photosAmt) {
    override suspend fun fetchPhotos(
        lastPhotoDate: OffsetDateTime,
        lastPhotoId: Long
    ): List<Photo> {
        return eventRepository.getPhotosOfEventPaginated(eventId, lastPhotoDate, lastPhotoId, photosAmt)
    }
}