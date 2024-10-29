package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.repositories.TagRepository

class GetAllSearchedCheckableTagsUseCase(private val tagRepository: TagRepository, private val searchedTag: String): GetItemsUseCase<CheckableItem<Tag>> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<CheckableItem<Tag>> {
        TODO("Not yet implemented")
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}