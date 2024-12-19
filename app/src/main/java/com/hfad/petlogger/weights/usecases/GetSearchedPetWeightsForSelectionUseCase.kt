package com.hfad.petlogger.weights.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.weights.PetWeightForSelection
import com.hfad.petlogger.weights.WeightDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSearchedPetWeightsForSelectionUseCase(
    private val weightDao: WeightDao,
    private val weightsAmt: Int,
    pet: LiveData<Pet>
): GetSearchedItemsUseCase<PetWeightForSelection> {
    private var lastWeightDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastWeightId = Long.MAX_VALUE
    override var currentQuery: String = ""
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    private val buildQuery = BuildWeightSearchQueryUseCase(
        weightsAmt = weightsAmt,
        parseSearchQuery = ParseSearchQueryUseCase(listOf("before", "after")),
        getWeightFor = BuildWeightSearchQueryUseCase.GetWeightFor.PetSelectionList,
        pickFrom = BuildWeightSearchQueryUseCase.Pick.FromPet(pet)
    )

    override suspend fun invoke(): List<PetWeightForSelection> = withContext(Dispatchers.IO) {
        val builtQuery = buildQuery(query = currentQuery, lastWeightDate = lastWeightDate, lastWeightId = lastWeightId)
        builtQuery?.let { query ->
            val weightsFetched = weightDao.searchWeightsOfPetForSelectionList(query)
            lastWeightId = weightsFetched.lastOrNull()?.weightId ?: Long.MAX_VALUE
            lastWeightDate = weightsFetched.lastOrNull()?.weightDateTime ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
            _onLastPage = weightsFetched.size < weightsAmt
            return@withContext weightsFetched.map { it.toPetWeightForSelection() }
        }
        listOf()
    }

    override fun resetCurrentPoint() {
        lastWeightDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastWeightId = Long.MAX_VALUE
        _onLastPage = false
    }
}