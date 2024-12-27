package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetWithProfilePic

class GetMorePetsOfNoteUseCase(
    private val noteRepository: NoteRepository,
    private val noteId: Long,
    private val petsAmt: Int
): GetPaginatedPetsUseCase(petsAmt) {
    override suspend fun fetchPets(lastPetId: Long): List<PetWithProfilePic> {
        return noteRepository.getPetsOfNotePaginated(noteId, lastPetId, petsAmt)
    }
}