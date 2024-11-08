package com.hfad.petlogger.tags.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.tags.TagRepository

class GetSearchedTagsUseCase(private val tagRepository: TagRepository, private val query: String):
    GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return tagRepository.searchTagsByQuery(query)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}