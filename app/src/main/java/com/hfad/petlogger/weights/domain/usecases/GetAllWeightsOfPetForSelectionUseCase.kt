package com.hfad.petlogger.weights.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.weights.data.PetWeightForSelection
import com.hfad.petlogger.weights.data.WeightDao

class GetAllWeightsOfPetForSelectionUseCase(
    private val weightDao: WeightDao,
    private val petId: Long,
    private val weightsAmt: Int
): GetItemsUseCase<PetWeightForSelection> {
    private var lastWeightDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastWeightId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<PetWeightForSelection> {
        val weights = weightDao.getPetWeightsForSelection(petId, lastWeightDate, lastWeightId, weightsAmt)
        lastWeightId = weights.lastOrNull()?.weightId ?: Long.MAX_VALUE
        lastWeightDate = weights.lastOrNull()?.weightDateTime ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = weights.size < weightsAmt
        return weights.map{it.toPetWeightForSelection()}
    }

    override fun resetCurrentPoint() {
        lastWeightDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastWeightId = Long.MAX_VALUE
        _onLastPage = false
    }
}