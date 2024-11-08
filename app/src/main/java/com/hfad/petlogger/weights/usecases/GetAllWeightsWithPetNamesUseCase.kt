package com.hfad.petlogger.weights.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.weights.WeightRepository
import com.hfad.petlogger.weights.WeightWithPetName

class GetAllWeightsWithPetNamesUseCase(private val weightRepository: WeightRepository):
    GetItemsUseCase<WeightWithPetName> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<WeightWithPetName> {
        return weightRepository.getAllWithPetNames()
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}