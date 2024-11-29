package com.hfad.petlogger.weights.usecases

import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.weights.PetWeightForSelection

class GetCheckableWeightsOfPetUseCase(private val petRepository: PetRepository, private val petId: Long):
    GetItemsUseCase<CheckableItem<PetWeightForSelection>> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<CheckableItem<PetWeightForSelection>> {
        return petRepository.getCheckablePetWeightsWithTextFields(petId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}