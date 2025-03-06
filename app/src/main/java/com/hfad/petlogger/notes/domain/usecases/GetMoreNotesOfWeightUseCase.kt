package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.weights.domain.WeightRepository
import java.time.OffsetDateTime

class GetMoreNotesOfWeightUseCase(
    private val weightRepository: WeightRepository,
    private val weightId: Long,
    private val notesAmt: Int
): GetPaginatedNotesUseCase(notesAmt) {
    override suspend fun fetchNotes(
        lastNoteUpdateDate: OffsetDateTime,
        lastNoteId: Long
    ): List<Note> {
        return weightRepository.getNotesOfWeightPaginated(weightId, lastNoteUpdateDate, lastNoteId, notesAmt)
    }
}