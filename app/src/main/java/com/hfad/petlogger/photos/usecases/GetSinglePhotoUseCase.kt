package com.hfad.petlogger.photos.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.photos.PhotoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSinglePhotoUseCase(private val photoDao: PhotoDao, private val photoId: Long): GetItemsUseCase<Photo> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Photo> = withContext(Dispatchers.IO) {
        listOf(photoDao.getPhoto(photoId))
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}