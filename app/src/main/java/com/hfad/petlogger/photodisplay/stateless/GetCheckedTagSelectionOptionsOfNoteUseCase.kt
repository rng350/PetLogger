package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.repositories.TagRepository

class GetCheckedTagSelectionOptionsOfNoteUseCase(private val tagRepository: TagRepository, private val noteId: Long? = null): GetItemsUseCase<CheckableItem<Tag>> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<CheckableItem<Tag>> {
        return tagRepository.getCheckedTagSelectionOptionsOfNote(noteId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }

}