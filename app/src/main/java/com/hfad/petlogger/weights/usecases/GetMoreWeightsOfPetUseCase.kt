package com.hfad.petlogger.weights.usecases

import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.weights.Weight

class GetMoreWeightsOfPetUseCase(private val petRepository: PetRepository,
                                 private val petId: Long,
                                 private val weightsAmt: Int
): GetItemsUseCase<Weight> {
    private var lastWeightDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastWeightId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Weight> {
        val weights = petRepository.getPetWeightsPaginated(petId, lastWeightDate, lastWeightId, weightsAmt)
        lastWeightId = weights.lastOrNull()?.id ?: Long.MAX_VALUE
        lastWeightDate = weights.lastOrNull()?.weightDateTime ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = weights.size < weightsAmt
        return weights
    }

    override fun resetCurrentPoint() {
        lastWeightDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastWeightId = Long.MAX_VALUE
        _onLastPage = false
    }
}