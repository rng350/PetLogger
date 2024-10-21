package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.PetRepository

class GetNotesOfPetUseCase(private val petRepository: PetRepository, private val petId: Long): GetItemsUseCase<Note> {
    override val onLastPage: Boolean = false

    override suspend fun invoke(): List<Note> {
        return petRepository.getNotesOfPet(petId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}