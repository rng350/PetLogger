package com.hfad.petlogger.tags.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.usecases.factories.GetSearchedCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.tags.TagRepository

class GetSearchedTagsFromCurrentSelectionUseCaseFactory(private val tagRepository: TagRepository): GetSearchedCurrentSelectionUseCaseFactory<Tag> {
    override fun createGetSearchedCurrentSelectionUseCase(currentSelection: LiveData<List<Tag>>): GetSearchedItemsUseCase<Tag> {
        return GetSearchedTagsFromCurrentSelectionUseCase(tagRepository, currentSelection)
    }
}