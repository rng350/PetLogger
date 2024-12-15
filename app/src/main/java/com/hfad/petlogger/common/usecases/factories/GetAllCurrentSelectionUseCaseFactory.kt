package com.hfad.petlogger.common.usecases.factories

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetItemsUseCase

// to get around circular dependency issue between said use cases & SelectionPickers
interface GetAllCurrentSelectionUseCaseFactory<T> {
    fun createGetAllCurrentSelectionUseCase(currentSelection: LiveData<List<T>>): GetItemsUseCase<T>
}