package com.hfad.petlogger.photos.usecases

import com.hfad.petlogger.events.EventRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetPhotosOfEventUseCase(private val eventId: Long, private val eventRepository: EventRepository):
    GetItemsUseCase<Photo> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<Photo> = withContext(Dispatchers.IO) {
        eventRepository.getPhotosOfEvent(eventId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}