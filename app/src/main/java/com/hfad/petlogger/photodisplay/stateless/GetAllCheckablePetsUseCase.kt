package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.PetRepository

class GetAllCheckablePetsUseCase(private val petRepository: PetRepository, private val initialPetSelection: List<Long>): GetItemsUseCase<CheckableItem<PetWithProfilePic>> {
    override suspend operator fun invoke(): List<CheckableItem<PetWithProfilePic>> {
        return petRepository.getCheckablePets(initialPetSelection)
    }
}