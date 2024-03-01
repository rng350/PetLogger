package com.hfad.petlogger.fetchers

import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.WeightDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchWeightDetailsUseCase {
    suspend operator fun invoke(weightDao: WeightDao, weightID: Long): WeightDetails = withContext(Dispatchers.IO) {
        weightDao.getWeightDetails(weightID)
    }
}