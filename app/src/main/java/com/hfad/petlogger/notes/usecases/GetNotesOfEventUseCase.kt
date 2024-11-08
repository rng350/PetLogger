package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.events.EventRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase

class GetNotesOfEventUseCase(private val eventRepository: EventRepository, private val eventId: Long):
    GetItemsUseCase<Note> {
    override val onLastPage: Boolean = false

    override suspend fun invoke(): List<Note> {
        return eventRepository.getNotesOfEvent(eventId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}