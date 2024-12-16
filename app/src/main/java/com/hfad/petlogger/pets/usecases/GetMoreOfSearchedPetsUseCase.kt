package com.hfad.petlogger.pets.usecases

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.pets.PetDao
import com.hfad.petlogger.pets.PetWithProfilePic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMoreOfSearchedPetsUseCase(
    private val petDao: PetDao,
    private val petsAmt: Int,
    pickFrom: BuildPetSearchQueryUseCase.Pick? = null
): GetSearchedItemsUseCase<PetWithProfilePic> {
    override var currentQuery: String = ""
    private var _onLastPage = false
    override val onLastPage: Boolean get() = _onLastPage
    private var lastPetId = Long.MIN_VALUE
    private val queryBuilder = BuildPetSearchQueryUseCase(petsAmt = petsAmt, pickFrom =  pickFrom)

    override suspend fun invoke(): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        val queryBuilt = queryBuilder(currentQuery, lastPetId)
        queryBuilt?.let { query ->
            val petsFetched = petDao.searchPets(query)
            lastPetId = petsFetched.lastOrNull()?.petId ?: Long.MAX_VALUE
            _onLastPage = petsFetched.size < petsAmt
            return@withContext petsFetched
        }
        listOf()
    }

    override fun resetCurrentPoint() {
        lastPetId = Long.MIN_VALUE
        _onLastPage = false
    }
}