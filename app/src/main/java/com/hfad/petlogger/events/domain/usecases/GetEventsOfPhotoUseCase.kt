package com.hfad.petlogger.events.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.data.EventForList
import com.hfad.petlogger.photos.domain.MediaRepository

class GetEventsOfPhotoUseCase(private val mediaRepository: MediaRepository, private val photoId: Long):
    GetItemsUseCase<EventForList> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<EventForList> {
        return mediaRepository.getEventsOfPhoto(photoId).map{it.toEventForList()}
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}