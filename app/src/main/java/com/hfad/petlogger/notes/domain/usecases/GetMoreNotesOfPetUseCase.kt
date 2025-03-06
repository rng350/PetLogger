package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.pets.domain.PetRepository
import java.time.OffsetDateTime

class GetMoreNotesOfPetUseCase(
    private val petRepository: PetRepository,
    private val petId: Long,
    private val notesAmt: Int
): GetPaginatedNotesUseCase(notesAmt) {
    override suspend fun fetchNotes(
        lastNoteUpdateDate: OffsetDateTime,
        lastNoteId: Long
    ): List<Note> {
        return petRepository.getPetNotesPaginated(petId, lastNoteUpdateDate, lastNoteId, notesAmt)
    }
}