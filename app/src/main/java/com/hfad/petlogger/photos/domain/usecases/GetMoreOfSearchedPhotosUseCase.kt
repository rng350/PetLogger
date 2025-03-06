package com.hfad.petlogger.photos.domain.usecases

import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.photos.data.PhotoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMoreOfSearchedPhotosUseCase(
    private val photoDao: PhotoDao,
    private val photosAmt: Int,
    pickFrom: BuildPhotoSearchQueryUseCase.Pick? = null
): GetSearchedItemsUseCase<Photo> {
    private var lastPhotoDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastPhotoId = Long.MAX_VALUE
    override var currentQuery: String = ""
    private var _onLastPage = false
    override val onLastPage: Boolean get() = _onLastPage
    private val queryBuilder = BuildPhotoSearchQueryUseCase(photosAmt = photosAmt, pickFrom =  pickFrom)

    override suspend fun invoke(): List<Photo> = withContext(Dispatchers.IO) {
        val queryBuilt = queryBuilder(currentQuery, lastPhotoDate, lastPhotoId)
        queryBuilt?.let {
            val photosFetched = photoDao.searchPhotos(queryBuilt)
            lastPhotoId = photosFetched.lastOrNull()?.id ?: Long.MAX_VALUE
            lastPhotoDate = photosFetched.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
            _onLastPage = photosFetched.size < photosAmt
            return@withContext photosFetched
        }
        listOf()
    }

    override fun resetCurrentPoint() {
        lastPhotoDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastPhotoId = Long.MAX_VALUE
        _onLastPage = false
    }
}