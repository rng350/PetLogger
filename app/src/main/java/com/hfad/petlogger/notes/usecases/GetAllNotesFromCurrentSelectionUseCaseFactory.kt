package com.hfad.petlogger.notes.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.factories.GetAllCurrentSelectionUseCaseFactory
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.notes.Note

class GetAllNotesFromCurrentSelectionUseCaseFactory: GetAllCurrentSelectionUseCaseFactory<Note> {
    override fun createGetAllCurrentSelectionUseCase(currentSelection: LiveData<List<Note>>): GetItemsUseCase<Note> {
        return GetAllNotesFromCurrentSelectionUseCase(currentSelection)
    }
}