package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import java.time.OffsetDateTime

class GetMoreOfSearchedNotesFromAllUseCase(
    private val noteRepository: NoteRepository,
    private val noteAmt: Int
): GetSearchedItemsUseCase<Note> {
    override var currentQuery: String = ""
    private var lastNoteUpdateDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastNoteId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Note> {
        val notes = noteRepository.getSearchedNotesFromAllPaginated(currentQuery, lastNoteUpdateDate, lastNoteId, noteAmt)
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