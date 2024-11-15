package com.hfad.petlogger.weights.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.weights.WeightDao
import com.hfad.petlogger.weights.WeightWithPetName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSingleWeightUseCase(private val weightDao: WeightDao, private val weightId: Long): GetSingleItemUseCase<WeightWithPetName> {

    override suspend fun invoke(): WeightWithPetName = withContext(Dispatchers.IO) {
        val weightDetails = weightDao.getWeightWithPetName(weightId)
        WeightWithPetName(weightDetails.weight, weightDetails.assocPet.petName)
    }

}