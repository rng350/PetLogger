package com.hfad.petlogger.tags.domain.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.data.Tag

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