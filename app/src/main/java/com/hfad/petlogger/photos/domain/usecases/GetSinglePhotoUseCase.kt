package com.hfad.petlogger.photos.domain.usecases

import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.photos.data.PhotoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSinglePhotoUseCase(private val photoDao: PhotoDao, private val photoId: Long): GetSingleItemUseCase<Photo> {
    override suspend fun invoke(): Photo = withContext(Dispatchers.IO) {
        photoDao.getPhoto(photoId)
    }
}