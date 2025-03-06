package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.notes.data.Note
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