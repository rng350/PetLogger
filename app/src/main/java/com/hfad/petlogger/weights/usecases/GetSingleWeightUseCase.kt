package com.hfad.petlogger.weights.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.weights.WeightDao
import com.hfad.petlogger.weights.WeightWithPetName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSingleWeightUseCase(private val weightDao: WeightDao, private val weightId: Long): GetItemsUseCase<WeightWithPetName> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<WeightWithPetName> = withContext(Dispatchers.IO) {
        val weightDetails = weightDao.getWeightWithPetName(weightId)
        listOf(WeightWithPetName(weightDetails.weight, weightDetails.assocPet.petName))
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }

}