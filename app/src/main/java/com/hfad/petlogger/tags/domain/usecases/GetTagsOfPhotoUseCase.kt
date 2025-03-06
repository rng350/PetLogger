package com.hfad.petlogger.tags.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.tags.data.Tag

class GetTagsOfPhotoUseCase(private val mediaRepository: MediaRepository, private val photoId: Long):
    GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return mediaRepository.getTagsOfPhoto(photoId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}