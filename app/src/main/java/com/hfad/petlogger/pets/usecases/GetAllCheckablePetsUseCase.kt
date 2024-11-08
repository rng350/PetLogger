package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.pets.PetWithProfilePic

class GetAllCheckablePetsUseCase(private val petRepository: PetRepository, private val initialPetSelection: List<Long>):
    GetItemsUseCase<CheckableItem<PetWithProfilePic>> {
    override val onLastPage: Boolean = false
    override suspend operator fun invoke(): List<CheckableItem<PetWithProfilePic>> {
        return petRepository.getCheckablePets(initialPetSelection)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}