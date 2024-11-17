package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.notes.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSingleNoteUseCase(private val noteDao: NoteDao, private val noteId: Long): GetSingleItemUseCase<Note> {
    override suspend fun invoke(): Note = withContext(Dispatchers.IO) {
       noteDao.get(noteId)
    }
}