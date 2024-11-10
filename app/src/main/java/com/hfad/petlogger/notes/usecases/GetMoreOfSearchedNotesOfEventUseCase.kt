package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.events.EventRepository
import com.hfad.petlogger.notes.Note
import java.time.OffsetDateTime

class GetMoreOfSearchedNotesOfEventUseCase(private val eventRepository: EventRepository, private val eventId: Long, private val notesAmt: Int): GetSearchedItemsUseCase<Note> {
    private var lastNoteEditedDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastNoteId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override var currentQuery: String = ""

    override suspend fun invoke(): List<Note> {
        val notes = eventRepository.getSearchedNotesOfEvent(eventId, currentQuery, lastNoteEditedDate, lastNoteId, notesAmt)
        lastNoteEditedDate = notes.lastOrNull()?.lastUpdated ?: OffsetDateTime.MIN
        lastNoteId = notes.lastOrNull()?.id ?: Long.MIN_VALUE
        _onLastPage = notes.size < notesAmt
        return notes
    }

    override fun resetCurrentPoint() {
        lastNoteEditedDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastNoteId = Long.MAX_VALUE
        _onLastPage = false
    }
}