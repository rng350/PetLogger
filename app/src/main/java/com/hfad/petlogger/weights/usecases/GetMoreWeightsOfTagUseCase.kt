package com.hfad.petlogger.weights.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.weights.WeightDao
import com.hfad.petlogger.weights.WeightForList

class GetMoreWeightsOfTagUseCase(
    private val weightDao: WeightDao,
    private val tagId: Long,
    private val weightsAmt: Int
): GetItemsUseCase<WeightForList> {
    private var lastWeightDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastWeightId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<WeightForList> {
        val weights = weightDao.getAllWeightsOfTagPaginated(tagId, lastWeightDate, lastWeightId, weightsAmt)
        lastWeightId = weights.lastOrNull()?.weightId ?: Long.MAX_VALUE
        lastWeightDate = weights.lastOrNull()?.weightDateTime ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = weights.size < weightsAmt
        return weights.map{it.toWeightForList()}
    }

    override fun resetCurrentPoint() {
        lastWeightDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastWeightId = Long.MAX_VALUE
        _onLastPage = false
    }
}