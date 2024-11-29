package com.hfad.petlogger.weights.usecases

import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.weights.WeightDao
import com.hfad.petlogger.weights.WeightForList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSearchedWeightsFromAllForDisplayUseCase(
    private val weightDao: WeightDao,
    private val weightsAmt: Int
): GetSearchedItemsUseCase<WeightForList> {
    private var lastWeightDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastWeightId = Long.MAX_VALUE
    private var _onLastPage = false
    override var currentQuery: String = ""
    private val buildQuery = BuildWeightSearchQueryUseCase(
        weightsAmt = weightsAmt,
        parseSearchQuery = ParseSearchQueryUseCase(listOf("pet", "before", "after")),
        getWeightFor = BuildWeightSearchQueryUseCase.GetWeightFor.GeneralDisplayList,
        pickFrom = null
    )
    override val onLastPage: Boolean get() = _onLastPage

    override suspend fun invoke(): List<WeightForList> = withContext(Dispatchers.IO) {
        val builtQuery = buildQuery(query = currentQuery, lastWeightDate = lastWeightDate, lastWeightId = lastWeightId)
        builtQuery?.let { query ->
            val weightsFetched = weightDao.getWeightForGeneralDisplayList(query)
            lastWeightId = weightsFetched.lastOrNull()?.weightId ?: Long.MAX_VALUE
            lastWeightDate = weightsFetched.lastOrNull()?.weightDateTime ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
            _onLastPage = weightsFetched.size < weightsAmt
            return@withContext weightsFetched.map { it.toWeightForList() }
        }
        listOf()
    }

    override fun resetCurrentPoint() {
        lastWeightDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastWeightId = Long.MAX_VALUE
        _onLastPage = false
    }
}