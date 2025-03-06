package com.hfad.petlogger.events.domain.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.data.EventForList

// No pagination at the moment, I'd think of this as an adapter
class GetAllEventsFromCurrentSelectionUseCase(
    val currentSelection: LiveData<List<EventForList>>
): GetItemsUseCase<EventForList> {
    private var _onLastPage = false
    override val onLastPage: Boolean get() = _onLastPage

    override suspend fun invoke(): List<EventForList> {
        return currentSelection.value ?: listOf()
    }

    override fun resetCurrentPoint() {
    }
}