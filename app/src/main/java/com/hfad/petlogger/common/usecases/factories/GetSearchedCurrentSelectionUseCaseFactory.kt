package com.hfad.petlogger.common.usecases.factories

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase

// to get around circular dependency issue between said use cases & SelectionPickers
interface GetSearchedCurrentSelectionUseCaseFactory<T> {
    fun createGetSearchedCurrentSelectionUseCase(currentSelection: LiveData<List<T>>): GetSearchedItemsUseCase<T>
}