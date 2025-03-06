package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.pets.domain.PetRepository

class GetNotesOfPetUseCase(private val petRepository: PetRepository, private val petId: Long):
    GetItemsUseCase<Note> {
    override val onLastPage: Boolean = false

    override suspend fun invoke(): List<Note> {
        return petRepository.getNotesOfPet(petId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}