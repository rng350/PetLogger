package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.photodisplay.stateful.GetItemsForDisplayUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AssociatedNotesDisplayViewModel (getNotesForDisplay: GetItemsForDisplayUseCase<Note>) : ViewModel() {
    val events: StateFlow<List<Note>> = getNotesForDisplay().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf<Note>()
    )
    val noteNavigator = Navigator()
    companion object {
        fun provideFactory(getNotesForDisplay: GetItemsForDisplayUseCase<Note>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedNotesDisplayViewModel::class.java)) {
                    return AssociatedNotesDisplayViewModel(getNotesForDisplay) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}