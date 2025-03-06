package com.hfad.petlogger.weights.domain.usecases

import com.hfad.petlogger.weights.data.WeightDao
import com.hfad.petlogger.weights.data.WeightForListFetched
import java.time.OffsetDateTime

class GetMoreWeightsOfTagUseCase(
    private val weightDao: WeightDao,
    private val tagId: Long,
    private val weightsAmt: Int
): GetPaginatedWeightsForGeneralDisplay(weightsAmt) {
    override suspend fun fetchWeights(
        lastWeightDate: OffsetDateTime,
        lastWeightId: Long
    ): List<WeightForListFetched> {
        return weightDao.getAllWeightsOfTagPaginated(tagId, lastWeightDate, lastWeightId, weightsAmt)
    }
}