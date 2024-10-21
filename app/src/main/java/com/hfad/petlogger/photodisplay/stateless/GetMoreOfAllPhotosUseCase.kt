package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.util.Constants

class GetMoreOfAllPhotosUseCase(
    private val mediaRepository: MediaRepository,
    private val photosAmt: Int
): GetItemsUseCase<Photo> {
    private var lastPhotoDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastPhotoId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Photo> {
        val photos = mediaRepository.getAllPhotosPaginated(lastPhotoDate, lastPhotoId, photosAmt)
        lastPhotoDate = photos.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastPhotoId = photos.lastOrNull()?.id ?: Long.MAX_VALUE
        _onLastPage = photos.size < photosAmt
        return photos
    }

    override fun resetCurrentPoint() {
        lastPhotoDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastPhotoId = Long.MAX_VALUE
        _onLastPage = false
    }
}