package com.hfad.petlogger.pets.domain.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.pets.data.PetDao
import com.hfad.petlogger.pets.data.PetWithProfilePic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class GetSearchedPetsFromCurrentSelectionUseCase(
    private val petDao: PetDao,
    private val currentSelection: LiveData<List<PetWithProfilePic>>
): GetSearchedItemsUseCase<PetWithProfilePic> {
    override var currentQuery: String = ""
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    private val queryBuilder = BuildPetSearchQueryUseCase(get = BuildPetSearchQueryUseCase.Get.OnlyIds)
    private var idsAlreadyFetched: Boolean = false
    private var fetchedIds: List<Long> = listOf()

    override suspend fun invoke(): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        currentSelection.value?.let {  currentPetSelection ->
            val currentSelectionMap = currentPetSelection.associateBy { it.petId }
            if (idsAlreadyFetched) {
                return@withContext fetchedIds.mapNotNull { currentSelectionMap[it] }
            }
            else {
                val builtQuery = queryBuilder(
                    query = currentQuery
                )
                builtQuery?.let { query ->
                    val searchResultsFetched = async {
                        petDao.searchPetIds(query)
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