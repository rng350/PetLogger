package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetPhotosOfEventUseCase(private val eventId: Long, private val eventRepository: EventRepository): GetItemsUseCase<Photo> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<Photo> = withContext(Dispatchers.IO) {
        eventRepository.getPhotosOfEvent(eventId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}