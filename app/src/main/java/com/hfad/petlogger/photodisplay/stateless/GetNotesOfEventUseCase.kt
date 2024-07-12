package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.EventRepository

class GetNotesOfEventUseCase(private val eventRepository: EventRepository, private val eventId: Long): GetItemsUseCase<Note> {
    override suspend fun invoke(): List<Note> {
        return eventRepository.getNotesOfEvent(eventId)
    }
}