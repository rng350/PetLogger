package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.repositories.EventRepository
import kotlinx.coroutines.flow.Flow

class GetAllEventsForDisplayUseCase(private val eventRepository: EventRepository): GetItemsForDisplayUseCase<Event> {
    override fun invoke(): Flow<List<Event>> {
        return eventRepository.getAllEventsAsFlow()
    }
}