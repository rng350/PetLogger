package com.hfad.petlogger.screens.note.notemultiselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.notes.domain.usecases.GetAllNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfAllNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfSearchedNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetSearchedNotesFromCurrentSelectionUseCaseFactory
import kotlinx.coroutines.launch

class NoteMultiSelectionViewModel(
    getAllNotes: GetMoreOfAllNotesUseCase,
    getInitialSelection: GetMultipleInitialItemsUseCase<Note>? = null,
    getSearchedSelectionOptions: GetMoreOfSearchedNotesUseCase,
    getAllNotesFromCurrentSelectionFactory: GetAllNotesFromCurrentSelectionUseCaseFactory,
    getSearchedNotesFromCurrentSelectionFactory: GetSearchedNotesFromCurrentSelectionUseCaseFactory
) : ViewModel() {
    val selectionTracker = MultiSelectionTracker<Note>(
        getAllSelectionOptions = getAllNotes,
        getInitialSelection = getInitialSelection,
        getSearchedSelectionOptions = getSearchedSelectionOptions,
        getAllCurrentSelectionDisplayFactory = getAllNotesFromCurrentSelectionFactory,
        getSearchedCurrentSelectionDisplayFactory = getSearchedNotesFromCurrentSelectionFactory,
        coroutineScope = viewModelScope
    )
    private var _currentSelectionChanged = false
    val currentSelectionChanged get() = _currentSelectionChanged
    private var visibleSelectionOptionsLoading: Boolean = false

    fun resetSelection() {
        selectionTracker.resetSelection()
    }

    fun getNotesToAdd(): List<Note> {
        return selectionTracker.getSelectionToAdd().map{it}
    }

    fun getNotesToRemove(): List<Note> {
        return selectionTracker.getSelectionToRemove().map{it}
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

    fun reset() {
        selectionTracker.resetSelection()
    }

    fun cancel() {
        selectionTracker.cancelProspectiveSelection()
    }

    companion object {
        fun provideFactory(
            getAllNotes: GetMoreOfAllNotesUseCase,
            getInitialSelection: GetMultipleInitialItemsUseCase<Note>? = null,
            getSearchedSelectionOptions: GetMoreOfSearchedNotesUseCase,
            getAllNotesFromCurrentSelectionFactory: GetAllNotesFromCurrentSelectionUseCaseFactory,
            getSearchedNotesFromCurrentSelectionFactory: GetSearchedNotesFromCurrentSelectionUseCaseFactory
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NoteMultiSelectionViewModel::class.java)) {
                    return NoteMultiSelectionViewModel(
                        getAllNotes,
                        getInitialSelection,
                        getSearchedSelectionOptions,
                        getAllNotesFromCurrentSelectionFactory,
                        getSearchedNotesFromCurrentSelectionFactory) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}