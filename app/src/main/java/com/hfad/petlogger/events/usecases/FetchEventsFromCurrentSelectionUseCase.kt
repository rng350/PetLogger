package com.hfad.petlogger.events.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.EventForList

// No pagination at the moment, I'd think of this as an adapter
class FetchEventsFromCurrentSelectionUseCase(
    val currentSelection: LiveData<List<EventForList>>
): GetItemsUseCase<EventForList> {
    private var _onLastPage = false
    override val onLastPage: Boolean get() = _onLastPage

    override suspend fun invoke(): List<EventForList> {
        val events = currentSelection.value ?: listOf()
        _onLastPage = true
        return events
    }

    override fun resetCurrentPoint() {
    }
}