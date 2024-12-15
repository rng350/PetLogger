package com.hfad.petlogger.tags.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.tags.TagRepository

class GetAllTagsFromCurrentSelectionUseCase(private val currentSelection: LiveData<List<Tag>>): GetItemsUseCase<Tag> {
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Tag> {
        currentSelection.value?.let { currentSelList ->
            _onLastPage = true
            return currentSelList
        }
        return listOf()
    }

    override fun resetCurrentPoint() {
        _onLastPage = false
    }
}