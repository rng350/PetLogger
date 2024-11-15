package com.hfad.petlogger.screens.note.notemultiselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase

class NoteMultiSelectionViewModel(getAllNotes: GetItemsUseCase<Note>, getInitialSelection: GetMultipleInitialItemsUseCase<Note>? = null) : ViewModel() {
    val selectionTracker = MultiSelectionTracker<Note>(
        allOptionsFetcher = getAllNotes,
        initialItemsUseCase = getInitialSelection,
        coroutineScope = viewModelScope
    )
    private var _currentSelectionChanged = false
    val currentSelectionChanged get() = _currentSelectionChanged

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

    fun reset() {
        selectionTracker.resetSelection()
    }

    fun cancel() {
        selectionTracker.cancelProspectiveSelection()
    }

    companion object {
        fun provideFactory(
            getAllNotes: GetItemsUseCase<Note>, getInitialSelection: GetMultipleInitialItemsUseCase<Note>? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NoteMultiSelectionViewModel::class.java)) {
                    return NoteMultiSelectionViewModel(getAllNotes, getInitialSelection) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}