package com.hfad.petlogger.notes.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.notes.Note

class GetAllNotesFromCurrentSelectionUseCase(
    val currentSelection: LiveData<List<Note>>
): GetItemsUseCase<Note> {
    private var _onLastPage = false
    override val onLastPage: Boolean get() = _onLastPage

    override suspend fun invoke(): List<Note> {
        return currentSelection.value ?: listOf()
    }

    override fun resetCurrentPoint() {
    }
}