package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.weights.WeightRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
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