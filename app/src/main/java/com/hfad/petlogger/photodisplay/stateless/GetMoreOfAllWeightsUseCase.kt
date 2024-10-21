package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.WeightForList
import com.hfad.petlogger.repositories.WeightRepository
import com.hfad.petlogger.util.Constants

class GetMoreOfAllWeightsUseCase(
    private val weightRepository: WeightRepository,
    private val weightsAmt: Int
): GetItemsUseCase<WeightForList> {
    private var lastWeightDateTime = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastWeightId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    override suspend fun invoke(): List<WeightForList> {
        val weights = weightRepository.getAllWeightsPaginated(lastWeightDateTime, lastWeightId, weightsAmt)
        lastWeightDateTime = weights.lastOrNull()?.weightDateTime ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastWeightId = weights.lastOrNull()?.weightId ?: Long.MAX_VALUE
        _onLastPage = weights.size < weightsAmt

        return weights.map { weight ->
            weight.toWeightForList()
        }
    }

    override fun resetCurrentPoint() {
        lastWeightDateTime = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastWeightId = Long.MAX_VALUE
        _onLastPage = false
    }
}