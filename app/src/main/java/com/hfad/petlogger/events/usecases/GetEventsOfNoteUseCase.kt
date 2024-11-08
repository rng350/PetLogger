package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.Event

class GetEventsOfNoteUseCase(private val noteRepository: NoteRepository, private val noteId: Long):
    GetItemsUseCase<Event> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<Event> {
        return noteRepository.getEventsOfNote(noteId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}