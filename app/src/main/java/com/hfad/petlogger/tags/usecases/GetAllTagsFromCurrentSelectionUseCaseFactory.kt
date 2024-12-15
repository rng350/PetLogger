package com.hfad.petlogger.tags.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.factories.GetAllCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.Tag

class GetAllTagsFromCurrentSelectionUseCaseFactory(): GetAllCurrentSelectionUseCaseFactory<Tag> {
    override fun createGetAllCurrentSelectionUseCase(currentSelection: LiveData<List<Tag>>): GetItemsUseCase<Tag> {
        return GetAllTagsFromCurrentSelectionUseCase(currentSelection)
    }
}