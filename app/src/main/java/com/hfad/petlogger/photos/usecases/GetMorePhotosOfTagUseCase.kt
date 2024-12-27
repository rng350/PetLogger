package com.hfad.petlogger.photos.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.photos.PhotoDao
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