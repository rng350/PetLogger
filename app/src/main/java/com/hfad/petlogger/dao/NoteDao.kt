package com.hfad.petlogger.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hfad.petlogger.entities.EventNote
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetNote
import com.hfad.petlogger.entities.PhotoNote
import com.hfad.petlogger.entities.WeightNote

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

    @Query("SELECT pet_table.pet_id AS pet_id, pet_name, pet_species, pet_breed, pet_sex, pet_dob, has_dob " +
            "FROM pet_table LEFT JOIN pet_note_table " +
            "WHERE pet_table.pet_id = pet_note_table.pet_id AND :noteId = pet_note_table.note_id")
    suspend fun getPetsOfNote(noteId: Long): List<Pet>

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

    @Insert
    suspend fun insert(weightNote: WeightNote)

    @Update
    suspend fun update(weightNote: WeightNote)

    @Delete
    suspend fun delete(weightNote: WeightNote)

    @Insert
    suspend fun insert(photoNote: PhotoNote)

    @Update
    suspend fun update(photoNote: PhotoNote)

    @Delete
    suspend fun delete(photoNote: PhotoNote)
}