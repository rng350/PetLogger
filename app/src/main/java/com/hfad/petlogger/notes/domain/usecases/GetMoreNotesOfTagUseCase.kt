package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.notes.data.NoteDao
import java.time.OffsetDateTime

class GetMoreNotesOfTagUseCase(
    private val noteDao: NoteDao,
    private val tagId: Long,
    private val notesAmt: Int
): GetPaginatedNotesUseCase(notesAmt) {
    override suspend fun fetchNotes(lastNoteUpdateDate: OffsetDateTime, lastNoteId: Long): List<Note>
        = noteDao.getAllNotesOfTagPaginated(tagId, lastNoteUpdateDate, lastNoteId, notesAmt)
}