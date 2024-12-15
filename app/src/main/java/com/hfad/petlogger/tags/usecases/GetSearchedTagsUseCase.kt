package com.hfad.petlogger.tags.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.tags.TagRepository

class GetSearchedTagsUseCase(
    private val tagRepository: TagRepository
): GetSearchedItemsUseCase<Tag> {
    override var currentQuery: String = ""
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Tag> {
        _onLastPage = true
        return tagRepository.searchTagsByQuery(currentQuery)
    }

    override fun resetCurrentPoint() {
        _onLastPage = false
    }
}