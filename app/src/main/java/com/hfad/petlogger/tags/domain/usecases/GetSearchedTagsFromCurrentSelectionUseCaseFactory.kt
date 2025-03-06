package com.hfad.petlogger.tags.domain.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.usecases.factories.GetSearchedCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.data.Tag
import com.hfad.petlogger.tags.domain.TagRepository

class GetSearchedTagsFromCurrentSelectionUseCaseFactory(private val tagRepository: TagRepository): GetSearchedCurrentSelectionUseCaseFactory<Tag> {
    override fun createGetSearchedCurrentSelectionUseCase(currentSelection: LiveData<List<Tag>>): GetSearchedItemsUseCase<Tag> {
        return GetSearchedTagsFromCurrentSelectionUseCase(tagRepository, currentSelection)
    }
}