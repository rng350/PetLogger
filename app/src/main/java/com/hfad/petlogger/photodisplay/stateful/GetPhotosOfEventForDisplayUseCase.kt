package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.EventRepository
import kotlinx.coroutines.flow.Flow

class GetPhotosOfEventForDisplayUseCase(private val eventId: Long, private val eventRepository: EventRepository):
    GetItemsForDisplayUseCase<Photo> {
    override fun invoke(): Flow<List<Photo>> {
        return eventRepository.getPhotosOfEventAsFlow(eventId)
    }
}