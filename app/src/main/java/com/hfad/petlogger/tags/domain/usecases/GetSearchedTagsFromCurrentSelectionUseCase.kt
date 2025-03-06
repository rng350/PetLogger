package com.hfad.petlogger.tags.domain.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.tags.data.Tag
import com.hfad.petlogger.tags.domain.TagRepository

class GetSearchedTagsFromCurrentSelectionUseCase(private val tagRepository: TagRepository, private val currentSelection: LiveData<List<Tag>>): GetSearchedItemsUseCase<Tag> {
    override var currentQuery: String = ""
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Tag> {
        _onLastPage = true
        currentSelection.value?.let { currentSelList ->
            val currentMap = currentSelList.associateBy { it.tagId }
            val fetchedTags = tagRepository.searchTagsByQuery(currentQuery)
            return fetchedTags.filter { currentMap.containsKey(it.tagId) }
        }
        return listOf()
    }

    override fun resetCurrentPoint() {
        _onLastPage = false
    }
}