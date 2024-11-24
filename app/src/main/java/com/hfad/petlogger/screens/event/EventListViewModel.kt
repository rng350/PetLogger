package com.hfad.petlogger.screens.event

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.events.EventForList
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.common.util.NewEntityNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventListViewModel(
    private val getInitialEvents: GetItemsUseCase<EventForList>,
    private val getSearchedEvents: GetSearchedItemsUseCase<EventForList>
) : ViewModel() {
    private var currentEventGetter: GetItemsUseCase<EventForList> = getInitialEvents
    private val _events: MutableStateFlow<List<EventForList>> = MutableStateFlow(listOf())
    val event: StateFlow<List<EventForList>> = _events.asStateFlow()
    val eventNavigator = Navigator()
    val newEventNavigator = NewEntityNavigator()
    private var isLoading: Boolean = false
    init {
        reload()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedEvents = currentEventGetter()
            _events.update { it + loadedEvents }
            isLoading = false
        }
    }
    private fun reload() {
        viewModelScope.launch {
            isLoading = true
            val loadedEvents = currentEventGetter()
            _events.update { loadedEvents }
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return currentEventGetter.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }

    fun onQueryTextSubmit(query: String?) {
        if (query != null) {
            reinitializeGetterType(query)
        } else {
            Log.d("EventListVM", "onQueryTextSubmit: Query is null")
        }
    }

    fun onQueryTextChanged(newText: String?) {
        if (newText != null) {
            reinitializeGetterType(newText)
        } else {
            Log.d("EventListVM", "onQueryTextChanged: Query is null")
        }
    }

    private fun reinitializeGetterType(query: String) {
        if (query.isNotEmpty()) {
            getSearchedEvents.changeSearchQueryAndResetCurrentPoint(query)
            currentEventGetter = getSearchedEvents
        } else {
            currentEventGetter = getInitialEvents
            currentEventGetter.resetCurrentPoint()
        }
        reload()
    }
    companion object {
        fun provideFactory(getInitialEvents: GetItemsUseCase<EventForList>, getSearchedEvents: GetSearchedItemsUseCase<EventForList>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EventListViewModel::class.java)) {
                    return EventListViewModel(getInitialEvents, getSearchedEvents) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }

}