package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.events.EventForList
import java.time.OffsetDateTime

class GetMoreEventsOfNoteUseCase(
    private val noteRepository: NoteRepository,
    private val noteId: Long,
    private val eventAmt: Int
): GetPaginatedEventsUseCase(eventAmt) {
    override suspend fun fetchEvents(lastEventDate: OffsetDateTime, lastEventId: Long): List<Event>
        = noteRepository.getNoteEventsAsListPaginated(noteId, lastEventDate, lastEventId, eventAmt)
}