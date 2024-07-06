package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetEventsOfNoteForDisplayUseCase(private val noteRepository: NoteRepository, private val noteId: Long): GetItemsForDisplayUseCase<Event> {
    override fun invoke(): Flow<List<Event>> {
        return noteRepository.getEventsOfNoteAsFlow(noteId)
    }
}