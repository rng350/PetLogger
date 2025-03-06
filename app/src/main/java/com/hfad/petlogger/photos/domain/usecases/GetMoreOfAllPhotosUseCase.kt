package com.hfad.petlogger.photos.domain.usecases

import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.photos.domain.MediaRepository
import java.time.OffsetDateTime

class GetMoreOfAllPhotosUseCase(
    private val mediaRepository: MediaRepository,
    private val photosAmt: Int
): GetPaginatedPhotosUseCase(photosAmt) {
    override suspend fun fetchPhotos(
        lastPhotoDate: OffsetDateTime,
        lastPhotoId: Long
    ): List<Photo> {
        return mediaRepository.getAllPhotosPaginated(lastPhotoDate, lastPhotoId, photosAmt)
    }
}