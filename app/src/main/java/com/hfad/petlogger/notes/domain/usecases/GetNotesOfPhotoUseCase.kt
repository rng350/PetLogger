package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.photos.domain.MediaRepository

class GetNotesOfPhotoUseCase(private val mediaRepository: MediaRepository, private val photoId: Long):
    GetItemsUseCase<Note> {
    override val onLastPage: Boolean = false

    override suspend fun invoke(): List<Note> {
        return mediaRepository.getNotesOfPhoto(photoId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}