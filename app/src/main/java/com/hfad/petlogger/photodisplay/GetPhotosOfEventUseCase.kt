package com.hfad.petlogger.photodisplay

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.EventRepository
import kotlinx.coroutines.flow.Flow

class GetPhotosOfEventUseCase(private val eventId: Long, private val eventRepository: EventRepository): GetAssociatedItemsUseCase<Photo> {
    override fun invoke(): Flow<List<Photo>> {
        return eventRepository.getPhotosOfEvent(eventId)
    }
}