package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.notes.data.Note
import java.time.OffsetDateTime

abstract class GetPaginatedNotesUseCase(
    private val notesAmt: Int
): GetItemsUseCase<Note> {
    private var lastNoteUpdateDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastNoteId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    override suspend fun invoke(): List<Note> {
        val notes = fetchNotes(lastNoteUpdateDate, lastNoteId)
        lastNoteId = notes.lastOrNull()?.id ?: Long.MAX_VALUE
        lastNoteUpdateDate = notes.lastOrNull()?.lastUpdated ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = notes.size < notesAmt
        return notes
    }

    override fun resetCurrentPoint() {
        lastNoteUpdateDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastNoteId = Long.MAX_VALUE
        _onLastPage = false
    }

    abstract suspend fun fetchNotes(lastNoteUpdateDate: OffsetDateTime, lastNoteId: Long): List<Note>
}