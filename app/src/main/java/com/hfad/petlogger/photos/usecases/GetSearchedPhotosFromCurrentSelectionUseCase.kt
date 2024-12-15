package com.hfad.petlogger.photos.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.photos.PhotoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class GetSearchedPhotosFromCurrentSelectionUseCase(
    private val photoDao: PhotoDao,
    private val currentSelection: LiveData<List<Photo>>
): GetSearchedItemsUseCase<Photo> {
    override var currentQuery: String = ""
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    private val queryBuilder = BuildPhotoSearchQueryUseCase(get = BuildPhotoSearchQueryUseCase.Get.Photo)
    private var idsAlreadyFetched: Boolean = false
    private var fetchedIds: List<Long> = listOf()

    override suspend fun invoke(): List<Photo> = withContext(Dispatchers.IO) {
        currentSelection.value?.let {  currentPhotoSelection ->
            val currentSelectionMap = currentPhotoSelection.associateBy { it.id }
            if (idsAlreadyFetched) {
                return@withContext fetchedIds.mapNotNull { currentSelectionMap[it] }
            }
            else {
                val builtQuery = queryBuilder(
                    query = currentQuery
                )
                builtQuery?.let { query ->
                    val searchResultsFetched = async {
                        photoDao.searchPhotoIds(query)
                    }
                    val searchResults = searchResultsFetched.await()
                    fetchedIds = searchResults
                    idsAlreadyFetched = true
                    return@withContext searchResults.mapNotNull { currentSelectionMap[it] }
                }
            }
        }
        listOf()
    }

    // will only get called if query changes
    override fun resetCurrentPoint() {
        _onLastPage = false
        idsAlreadyFetched = false
        fetchedIds = listOf()
    }
}