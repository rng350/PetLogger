package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.repositories.WeightRepository

class GetAllWeightsWithPetNamesUseCase(private val weightRepository: WeightRepository): GetItemsUseCase<WeightWithPetName> {
    override suspend fun invoke(): List<WeightWithPetName> {
        return weightRepository.getAllWithPetNames()
    }
}