package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.events.EventRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import java.time.OffsetDateTime

class GetMoreNotesOfEventUseCase(
    private val eventRepository: EventRepository,
    private val eventId: Long,
    private val amtLimit: Int
): GetPaginatedNotesUseCase(amtLimit) {
    override suspend fun fetchNotes(
        lastNoteUpdateDate: OffsetDateTime,
        lastNoteId: Long
    ): List<Note>
        = eventRepository.getNotesOfEventPaginated(eventId, lastNoteUpdateDate, lastNoteId, amtLimit)
}