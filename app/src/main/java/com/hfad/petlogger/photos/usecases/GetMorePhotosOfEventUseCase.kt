package com.hfad.petlogger.photos.usecases

import com.hfad.petlogger.events.EventRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.Photo
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