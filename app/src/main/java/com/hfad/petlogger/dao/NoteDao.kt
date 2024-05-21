package com.hfad.petlogger.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hfad.petlogger.entities.EventNote
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.PetNote

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_table WHERE note_id=:noteId")
    suspend fun get(noteId: Long): Note

    @Query("SELECT * FROM note_table")
    suspend fun getAll(): List<Note>

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Insert
    suspend fun insert(petNote: PetNote)

    @Update
    suspend fun update(petNote: PetNote)

    @Delete
    suspend fun delete(petNote: PetNote)

    @Insert
    suspend fun insert(eventNote: EventNote)

    @Update
    suspend fun update(eventNote: EventNote)

    @Delete
    suspend fun delete(eventNote: EventNote)
}