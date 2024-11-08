package com.hfad.petlogger.tags.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.tags.TagRepository

class GetAllTagsOfNoteAlphabeticalOrderUseCase(private val tagRepository: TagRepository, private val noteId: Long):
    GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return tagRepository.getTagsOfNoteAlphabeticalOrder(noteId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}