package com.hfad.petlogger.events.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.factories.GetSearchedCurrentSelectionUseCaseFactory
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.events.EventDao
import com.hfad.petlogger.events.EventForList

class GetSearchedEventsFromCurrentSelectionUseCaseFactory(private val eventDao: EventDao):
    GetSearchedCurrentSelectionUseCaseFactory<EventForList> {
    override fun createGetSearchedCurrentSelectionUseCase(currentSelection: LiveData<List<EventForList>>): GetSearchedItemsUseCase<EventForList> {
        return GetSearchedEventsFromCurrentSelectionUseCase(eventDao, currentSelection)
    }
}