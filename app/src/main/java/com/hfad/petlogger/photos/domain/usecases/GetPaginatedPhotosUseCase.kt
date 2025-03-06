package com.hfad.petlogger.photos.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.photos.data.Photo
import java.time.OffsetDateTime

abstract class GetPaginatedPhotosUseCase(private val photosAmt: Int): GetItemsUseCase<Photo> {
    private var lastPhotoDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastPhotoId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Photo> {
        val photos = fetchPhotos(lastPhotoDate, lastPhotoId)
        lastPhotoId = photos.lastOrNull()?.id ?: Long.MAX_VALUE
        lastPhotoDate = photos.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = photos.size < photosAmt
        return photos
    }

    override fun resetCurrentPoint() {
        lastPhotoDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastPhotoId = Long.MAX_VALUE
        _onLastPage = false
    }

    abstract suspend fun fetchPhotos(lastPhotoDate: OffsetDateTime, lastPhotoId: Long): List<Photo>
}