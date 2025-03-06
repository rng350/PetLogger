package com.hfad.petlogger.weights.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.weights.data.WeightWithPetName
import com.hfad.petlogger.weights.domain.WeightRepository

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