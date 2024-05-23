package com.hfad.petlogger.repositories

import com.hfad.petlogger.dao.NoteDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventNote
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.PetNote
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.Weight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class NoteRepository(
    private val noteDao: NoteDao,
    private val mediaRepository: MediaRepository
) {
    suspend fun getNote(noteId: Long): Note = withContext(Dispatchers.IO) {
        noteDao.get(noteId)
    }

    suspend fun getAllNotes(): List<Note> {
        return noteDao.getAll()
    }

    suspend fun insertNote(note: Note, events: List<Event>? = null, weights: List<Weight>? = null, photos: List<Photo>? = null): Long = withContext(Dispatchers.IO) {
        val noteId = noteDao.insert(note)
        photos?.let {
            for (photo in photos) {
                mediaRepository.addNotePhoto(photo, noteId)
            }
        }
        noteId
    }

    suspend fun insertPetNote(note: Note, petId: Long) {
        withContext(Dispatchers.IO) {
            val noteId = async {
                insertNote(note)
            }.await()
            noteDao.insert(PetNote(petId, noteId))
        }
    }

    suspend fun insertEventNote(note: Note, eventId: Long) {
        withContext(Dispatchers.IO) {
            val noteId = async {
                insertNote(note)
            }.await()
            noteDao.insert(EventNote(eventId, noteId))
        }
    }

    suspend fun insertWeightNote(note: Note, weightId: Long) {
    }

    suspend fun insertPhotoNote(note: Note, photoId: Long) {
    }
}