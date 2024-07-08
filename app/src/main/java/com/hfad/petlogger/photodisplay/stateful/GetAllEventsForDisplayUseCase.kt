package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.EventForList
import com.hfad.petlogger.repositories.EventRepository
import kotlinx.coroutines.flow.Flow

class GetAllEventsForDisplayUseCase(private val eventRepository: EventRepository): GetItemsForDisplayUseCase<EventForList> {
    override fun invoke(): Flow<List<EventForList>> {
        return eventRepository.getAllEventsAsFlow()
    }
}