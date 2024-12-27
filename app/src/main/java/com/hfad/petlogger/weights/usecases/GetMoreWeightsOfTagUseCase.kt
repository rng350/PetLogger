package com.hfad.petlogger.weights.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.weights.WeightDao
import com.hfad.petlogger.weights.WeightForList
import com.hfad.petlogger.weights.WeightForListFetched
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