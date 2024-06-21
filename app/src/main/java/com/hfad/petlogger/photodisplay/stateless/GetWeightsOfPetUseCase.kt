package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.entities.PetWeightForDisplay
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.repositories.PetRepository

class GetWeightsOfPetUseCase(private val petRepository: PetRepository, private val petId: Long): GetAssociatedItemsUseCase<CheckableItem<PetWeightForDisplay>> {
    override suspend fun invoke(): List<CheckableItem<PetWeightForDisplay>> {
        return petRepository.getPetWeightsWithTextFields(petId)
    }
}