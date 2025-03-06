package com.hfad.petlogger.photos.domain.usecases

import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.photos.data.PhotoDao
import java.time.OffsetDateTime

class GetMorePhotosOfTagUseCase(
    private val photoDao: PhotoDao,
    private val tagId: Long,
    private val photosAmt: Int
): GetPaginatedPhotosUseCase(photosAmt) {
    override suspend fun fetchPhotos(
        lastPhotoDate: OffsetDateTime,
        lastPhotoId: Long
    ): List<Photo> {
        return photoDao.getPhotosOfTagPaginated(tagId, lastPhotoDate, lastPhotoId, photosAmt)
    }
}