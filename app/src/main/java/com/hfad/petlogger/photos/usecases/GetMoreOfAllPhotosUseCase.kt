package com.hfad.petlogger.photos.usecases

import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.photos.Photo
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