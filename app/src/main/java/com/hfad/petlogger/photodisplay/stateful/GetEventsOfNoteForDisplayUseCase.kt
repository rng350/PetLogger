package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventForList
import com.hfad.petlogger.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetEventsOfNoteForDisplayUseCase(private val noteRepository: NoteRepository, private val noteId: Long): GetItemsForDisplayUseCase<EventForList> {
    override fun invoke(): Flow<List<EventForList>> {
        return noteRepository.getEventsOfNoteAsFlow(noteId)
    }
}