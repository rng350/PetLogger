package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import java.time.OffsetDateTime

class GetMoreOfAllNotesUseCase(
    private val noteRepository: NoteRepository,
    private val noteAmt: Int
): GetPaginatedNotesUseCase(noteAmt) {
    override suspend fun fetchNotes(
        lastNoteUpdateDate: OffsetDateTime,
        lastNoteId: Long
    ): List<Note> {
        return noteRepository.getAllNotesPaginated(lastNoteUpdateDate, lastNoteId, noteAmt)
    }
}