package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.EventRepository
import kotlinx.coroutines.flow.Flow

class GetNotesOfEventForDisplayUseCase(private val eventRepository: EventRepository, private val eventId: Long): GetItemsForDisplayUseCase<Note> {
    override fun invoke(): Flow<List<Note>> {
        return eventRepository.getNotesOfEventAsFlow(eventId)
    }
}