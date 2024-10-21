package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.repositories.WeightRepository

class GetAllWeightsWithPetNamesUseCase(private val weightRepository: WeightRepository): GetItemsUseCase<WeightWithPetName> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<WeightWithPetName> {
        return weightRepository.getAllWithPetNames()
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}