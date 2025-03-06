package com.hfad.petlogger.weights.domain.usecases

import com.hfad.petlogger.weights.data.WeightForListFetched
import com.hfad.petlogger.weights.domain.WeightRepository
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