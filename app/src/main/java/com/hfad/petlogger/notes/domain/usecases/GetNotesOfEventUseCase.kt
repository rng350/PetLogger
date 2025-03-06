package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.notes.data.Note

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