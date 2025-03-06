package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.notes.data.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSingleNoteUseCase(private val noteDao: NoteDao, private val noteId: Long): GetSingleItemUseCase<Note> {
    override suspend fun invoke(): Note = withContext(Dispatchers.IO) {
       noteDao.get(noteId)
    }
}