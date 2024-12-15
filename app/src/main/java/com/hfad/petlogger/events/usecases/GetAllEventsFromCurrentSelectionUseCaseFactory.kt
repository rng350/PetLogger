package com.hfad.petlogger.events.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.factories.GetAllCurrentSelectionUseCaseFactory
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.EventForList

class GetAllEventsFromCurrentSelectionUseCaseFactory:
    GetAllCurrentSelectionUseCaseFactory<EventForList> {
    override fun createGetAllCurrentSelectionUseCase(currentSelection: LiveData<List<EventForList>>): GetItemsUseCase<EventForList> {
        return GetAllEventsFromCurrentSelectionUseCase(currentSelection)
    }
}