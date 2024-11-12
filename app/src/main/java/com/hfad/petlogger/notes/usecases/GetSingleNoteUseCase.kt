package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.notes.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSingleNoteUseCase(private val noteDao: NoteDao, private val noteId: Long): GetItemsUseCase<Note> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Note> = withContext(Dispatchers.IO) {
       listOf(noteDao.get(noteId))
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}