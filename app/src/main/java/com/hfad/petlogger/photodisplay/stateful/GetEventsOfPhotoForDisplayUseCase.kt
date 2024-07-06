package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.repositories.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetEventsOfPhotoForDisplayUseCase(private val mediaRepository: MediaRepository, private val photoId: Long): GetItemsForDisplayUseCase<Event> {
    override fun invoke(): Flow<List<Event>> {
        return mediaRepository.getEventsOfPhoto(photoId)
    }
}