package com.hfad.petlogger.notes.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.factories.GetSearchedCurrentSelectionUseCaseFactory
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.notes.NoteDao

class GetSearchedNotesFromCurrentSelectionUseCaseFactory(private val noteDao: NoteDao):
    GetSearchedCurrentSelectionUseCaseFactory<Note> {
    override fun createGetSearchedCurrentSelectionUseCase(currentSelection: LiveData<List<Note>>): GetSearchedItemsUseCase<Note> {
        return GetSearchedNotesFromCurrentSelectionUseCase(noteDao, currentSelection)
    }
}