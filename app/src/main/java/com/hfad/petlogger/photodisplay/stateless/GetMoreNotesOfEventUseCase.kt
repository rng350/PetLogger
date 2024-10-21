package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.util.Constants

class GetMoreNotesOfEventUseCase(private val eventRepository: EventRepository,
                                 private val eventId: Long,
                                 private val amtLimit: Int
): GetItemsUseCase<Note> {
    private var lastNoteEditedDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastNoteId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Note> {
        val notes = eventRepository.getNotesOfEventPaginated(eventId, lastNoteEditedDate, lastNoteId, amtLimit)
        lastNoteId = notes.lastOrNull()?.id ?: Long.MAX_VALUE
        lastNoteEditedDate = notes.lastOrNull()?.lastUpdated ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = notes.size < amtLimit
        return notes
    }

    override fun resetCurrentPoint() {
        lastNoteEditedDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastNoteId = Long.MAX_VALUE
        _onLastPage = false
    }
}