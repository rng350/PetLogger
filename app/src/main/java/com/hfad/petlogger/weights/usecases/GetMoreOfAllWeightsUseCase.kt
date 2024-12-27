package com.hfad.petlogger.weights.usecases

import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.weights.WeightForList
import com.hfad.petlogger.weights.WeightForListFetched
import com.hfad.petlogger.weights.WeightRepository
import java.time.OffsetDateTime

class GetMoreOfAllWeightsUseCase(
    private val weightRepository: WeightRepository,
    private val weightsAmt: Int
): GetPaginatedWeightsForGeneralDisplay(weightsAmt) {
    override suspend fun fetchWeights(
        lastWeightDate: OffsetDateTime,
        lastWeightId: Long
    ): List<WeightForListFetched> {
        return weightRepository.getAllWeightsPaginated(lastWeightDate, lastWeightId, weightsAmt)
    }
}