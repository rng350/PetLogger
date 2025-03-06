package com.hfad.petlogger.screens.event.eventmultiselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.events.data.EventForList
import com.hfad.petlogger.events.domain.usecases.GetAllEventsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.events.domain.usecases.GetMoreOfAllEventsUseCase
import com.hfad.petlogger.events.domain.usecases.GetMoreOfSearchedEventsUseCase
import com.hfad.petlogger.events.domain.usecases.GetSearchedEventsFromCurrentSelectionUseCaseFactory
import kotlinx.coroutines.launch

class EventMultiSelectionViewModel(
    getAssociatedEvents: GetMultipleInitialItemsUseCase<EventForList>? = null,
    getAllEvents: GetMoreOfAllEventsUseCase,
    getSearchedEvents: GetMoreOfSearchedEventsUseCase,
    getAllEventsFromCurrentSelection: GetAllEventsFromCurrentSelectionUseCaseFactory,
    getSearchedEventsFromCurrentSelectionFactory: GetSearchedEventsFromCurrentSelectionUseCaseFactory
) : ViewModel() {
    val selectionTracker = MultiSelectionTracker<EventForList>(
        getAllSelectionOptions = getAllEvents,
        getInitialSelection = getAssociatedEvents,
        getSearchedSelectionOptions = getSearchedEvents,
        getAllCurrentSelectionDisplayFactory = getAllEventsFromCurrentSelection,
        getSearchedCurrentSelectionDisplayFactory = getSearchedEventsFromCurrentSelectionFactory,
        coroutineScope = viewModelScope
    )
    private var _currentSelectionChanged = false
    val currentSelectionChanged get() = _currentSelectionChanged
    private var visibleSelectionOptionsLoading: Boolean = false

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

    fun onCurrentSelectionDisplayQueryTextSubmit(query: String?) {
        query?.let {
            selectionTracker.searchFromCurrentSelectionDisplay(query)
        }
    }

    fun onCurrentSelectionDisplayQueryTextChange(newText: String?) {
        newText?.let {
            selectionTracker.searchFromCurrentSelectionDisplay(newText)
        }
    }

    fun onSelectionOptionsQueryTextSubmit(query: String?) {
        query?.let {
            selectionTracker.searchFromSelectionOptions(query)
        }
    }

    fun onSelectionOptionsQueryTextChange(newText: String?) {
        newText?.let {
            selectionTracker.searchFromSelectionOptions(newText)
        }
    }

    fun loadFromVisibleOptions() {
        viewModelScope.launch {
            visibleSelectionOptionsLoading = true
            selectionTracker.loadVisibleSelectionOptions()
            visibleSelectionOptionsLoading = false
        }
    }

    fun visibleOptionsAreLoading(): Boolean {
        return visibleSelectionOptionsLoading
    }

    fun visibleOptionsOnLastPage(): Boolean {
        return selectionTracker.visibleSelectionOptionsOnLastPage()
    }

    fun resetSelection() {
        selectionTracker.resetSelection()
    }

    fun cancel() {
        selectionTracker.cancelProspectiveSelection()
    }

    companion object {
        fun provideFactory(
            getAssociatedEvents: GetMultipleInitialItemsUseCase<EventForList>? = null,
            getAllEvents: GetMoreOfAllEventsUseCase,
            getSearchedEvents: GetMoreOfSearchedEventsUseCase,
            getAllEventsFromCurrentSelection: GetAllEventsFromCurrentSelectionUseCaseFactory,
            getSearchedEventsFromCurrentSelectionFactory: GetSearchedEventsFromCurrentSelectionUseCaseFactory
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EventMultiSelectionViewModel::class.java)) {
                    return EventMultiSelectionViewModel(
                        getAssociatedEvents,
                        getAllEvents,
                        getSearchedEvents,
                        getAllEventsFromCurrentSelection,
                        getSearchedEventsFromCurrentSelectionFactory) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}