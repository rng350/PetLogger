package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.repositories.MediaRepository

class GetAllTagsOfPhotoAlphabeticalOrderUseCase(private val mediaRepository: MediaRepository, private val photoId: Long): GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return mediaRepository.getTagsOfPhotoAlphabeticalOrder(photoId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}