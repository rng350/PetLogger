package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.photodisplay.stateful.GetItemsForDisplayUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AssociatedEventsDisplayViewModel(private val getAssociatedEvents: GetItemsForDisplayUseCase<Event>): ViewModel() {
    val events: StateFlow<List<Event>> = getAssociatedEvents().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf<Event>()
    )
    val eventNavigator = Navigator()

    companion object {
        fun provideFactory(getAssociatedEvents: GetItemsForDisplayUseCase<Event>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedEventsDisplayViewModel::class.java)) {
                    return AssociatedEventsDisplayViewModel(getAssociatedEvents) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}