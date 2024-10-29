package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.repositories.TagRepository

class getAllTagsOfNoteAlphabeticalOrderUseCase(private val tagRepository: TagRepository, private val noteId: Long): GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return tagRepository.getTagsOfNoteAlphabeticalOrder(noteId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}