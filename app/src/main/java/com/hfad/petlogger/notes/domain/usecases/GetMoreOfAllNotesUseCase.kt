package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.notes.domain.NoteRepository
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