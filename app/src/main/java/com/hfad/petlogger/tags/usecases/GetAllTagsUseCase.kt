package com.hfad.petlogger.tags.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.tags.TagRepository

class GetAllTagsUseCase(private val tagRepository: TagRepository): GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return tagRepository.getAllTags()
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}