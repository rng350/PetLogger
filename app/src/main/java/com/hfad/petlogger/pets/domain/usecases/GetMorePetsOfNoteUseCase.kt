package com.hfad.petlogger.pets.domain.usecases

import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.pets.data.PetWithProfilePic

class GetMorePetsOfNoteUseCase(
    private val noteRepository: NoteRepository,
    private val noteId: Long,
    private val petsAmt: Int
): GetPaginatedPetsUseCase(petsAmt) {
    override suspend fun fetchPets(lastPetId: Long): List<PetWithProfilePic> {
        return noteRepository.getPetsOfNotePaginated(noteId, lastPetId, petsAmt)
    }
}