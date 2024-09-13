package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.entities.PetWeightForDisplay
import com.hfad.petlogger.repositories.PetRepository

class GetCheckableWeightsOfPetUseCase(private val petRepository: PetRepository, private val petId: Long): GetItemsUseCase<CheckableItem<PetWeightForDisplay>> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<CheckableItem<PetWeightForDisplay>> {
        return petRepository.getCheckablePetWeightsWithTextFields(petId)
    }
}