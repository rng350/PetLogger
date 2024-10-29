package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.repositories.TagRepository

class GetSearchedTagsUseCase(private val tagRepository: TagRepository, private val query: String): GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return tagRepository.searchTagsByQuery(query)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}