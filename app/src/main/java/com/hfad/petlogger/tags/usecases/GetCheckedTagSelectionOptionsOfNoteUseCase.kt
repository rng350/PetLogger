package com.hfad.petlogger.tags.usecases

import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.tags.TagRepository

class GetCheckedTagSelectionOptionsOfNoteUseCase(private val tagRepository: TagRepository, private val noteId: Long? = null):
    GetItemsUseCase<CheckableItem<Tag>> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<CheckableItem<Tag>> {
        return tagRepository.getCheckedTagSelectionOptionsOfNote(noteId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }

}