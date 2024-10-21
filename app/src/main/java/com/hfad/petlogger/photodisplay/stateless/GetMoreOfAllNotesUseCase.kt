package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.util.Constants

class GetMoreOfAllNotesUseCase(
    private val noteRepository: NoteRepository,
    private val noteAmt: Int
): GetItemsUseCase<Note> {
    private var lastNoteUpdateDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastNoteId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Note> {
        val notes = noteRepository.getAllNotesPaginated(lastNoteUpdateDate, lastNoteId, noteAmt)
        lastNoteId = notes.lastOrNull()?.id ?: Long.MAX_VALUE
        lastNoteUpdateDate = notes.lastOrNull()?.lastUpdated ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = notes.size < noteAmt
        return notes
    }

    override fun resetCurrentPoint() {
        lastNoteUpdateDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastNoteId = Long.MAX_VALUE
        _onLastPage = false
    }
}