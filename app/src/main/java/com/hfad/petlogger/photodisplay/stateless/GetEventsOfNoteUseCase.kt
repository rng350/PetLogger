package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.repositories.NoteRepository

class GetEventsOfNoteUseCase(private val noteRepository: NoteRepository, private val noteId: Long): GetItemsUseCase<Event> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<Event> {
        return noteRepository.getEventsOfNote(noteId)
    }
}