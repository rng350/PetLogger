package com.hfad.petlogger.events.domain.usecases

import com.hfad.petlogger.events.data.Event
import com.hfad.petlogger.notes.domain.NoteRepository
import java.time.OffsetDateTime

class GetMoreEventsOfNoteUseCase(
    private val noteRepository: NoteRepository,
    private val noteId: Long,
    private val eventAmt: Int
): GetPaginatedEventsUseCase(eventAmt) {
    override suspend fun fetchEvents(lastEventDate: OffsetDateTime, lastEventId: Long): List<Event>
        = noteRepository.getNoteEventsAsListPaginated(noteId, lastEventDate, lastEventId, eventAmt)
}