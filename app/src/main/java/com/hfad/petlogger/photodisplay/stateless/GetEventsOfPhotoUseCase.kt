package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.repositories.MediaRepository

class GetEventsOfPhotoUseCase(private val mediaRepository: MediaRepository, private val photoId: Long): GetItemsUseCase<Event> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<Event> {
        return mediaRepository.getEventsOfPhoto(photoId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}