package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
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