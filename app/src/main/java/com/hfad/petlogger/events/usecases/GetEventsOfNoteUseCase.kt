package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.EventForList

class GetEventsOfNoteUseCase(private val noteRepository: NoteRepository, private val noteId: Long):
    GetItemsUseCase<EventForList> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<EventForList> {
        return noteRepository.getEventsOfNote(noteId).map{it.toEventForList()}
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}