package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.util.Constants

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
}