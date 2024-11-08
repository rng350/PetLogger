package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetWithProfilePic

class GetMorePetsOfNoteUseCase(
    private val noteRepository: NoteRepository,
    private val noteId: Long,
    private val petsAmt: Int
): GetItemsUseCase<PetWithProfilePic> {
    private var lastPetId = Long.MIN_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<PetWithProfilePic> {
        val pets = noteRepository.getPetsOfNotePaginated(noteId, lastPetId, petsAmt)
        lastPetId = pets.lastOrNull()?.petId ?: Long.MAX_VALUE
        _onLastPage = pets.size < petsAmt
        return pets
    }

    override fun resetCurrentPoint() {
        lastPetId = Long.MIN_VALUE
        _onLastPage = false
    }
}