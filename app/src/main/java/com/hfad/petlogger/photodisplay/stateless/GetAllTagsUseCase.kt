package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.repositories.TagRepository

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