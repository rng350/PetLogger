package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.MediaRepository

class GetNotesOfPhotoUseCase(private val mediaRepository: MediaRepository, private val photoId: Long): GetItemsUseCase<Note> {
    override val onLastPage: Boolean = false

    override suspend fun invoke(): List<Note> {
        return mediaRepository.getNotesOfPhoto(photoId)
    }
}