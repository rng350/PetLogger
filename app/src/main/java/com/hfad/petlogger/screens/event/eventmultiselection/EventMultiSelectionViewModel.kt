package com.hfad.petlogger.screens.event.eventmultiselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.EventForList

class EventMultiSelectionViewModel(
    getAllEvents: GetItemsUseCase<EventForList>,
    getAssociatedEvents: GetMultipleInitialItemsUseCase<EventForList>? = null
) : ViewModel() {
    val selectionTracker = MultiSelectionTracker<EventForList>(
        allOptionsFetcher = getAllEvents,
        initialItemsUseCase = getAssociatedEvents,
        coroutineScope = viewModelScope
    )
    private var _currentSelectionChanged = false
    val currentSelectionChanged get() = _currentSelectionChanged

    fun getEventsToAdd(): List<Long> {
        return selectionTracker.getSelectionToAdd().map{it.eventId}
    }

    fun getEventsToRemove(): List<Long> {
        return selectionTracker.getSelectionToRemove().map{it.eventId}
    }

    fun confirmSelection() {
        selectionTracker.confirmProspectiveSelection()
        _currentSelectionChanged = true
    }

    fun onCurrentSelectionChanged() {
        _currentSelectionChanged = false
    }

    fun resetSelection() {
        selectionTracker.resetSelection()
    }

    fun cancel() {
        selectionTracker.cancelProspectiveSelection()
    }

    companion object {
        fun provideFactory(
            getAllEvents: GetItemsUseCase<EventForList>,
            getAssociatedEvents: GetMultipleInitialItemsUseCase<EventForList>? = null)
        : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EventMultiSelectionViewModel::class.java)) {
                    return EventMultiSelectionViewModel(getAllEvents, getAssociatedEvents) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}