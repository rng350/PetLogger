package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.util.Constants
import java.time.OffsetDateTime

class GetMoreOfSearchedNotesFromAllUseCase(
    private val noteRepository: NoteRepository,
    private val noteAmt: Int,
    private val query: String
): GetItemsUseCase<Note> {
    private var lastNoteUpdateDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastNoteId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Note> {
        val notes = noteRepository.getSearchedNotesFromAllPaginated(query, lastNoteUpdateDate, lastNoteId, noteAmt)
        lastNoteUpdateDate = notes.lastOrNull()?.lastUpdated ?: OffsetDateTime.MIN
        lastNoteId = notes.lastOrNull()?.id ?: Long.MIN_VALUE
        _onLastPage = notes.size < noteAmt
        return notes
    }

    override fun resetCurrentPoint() {
        lastNoteUpdateDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastNoteId = Long.MAX_VALUE
        _onLastPage = false
    }
}