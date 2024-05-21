package com.hfad.petlogger.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "pet_note_table",
    primaryKeys = ["pet_id", "note_id"],
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = arrayOf("pet_id"),
            childColumns = arrayOf("pet_id"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Note::class,
            parentColumns = arrayOf("note_id"),
            childColumns = arrayOf("note_id"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class PetNote (
    @ColumnInfo(name="pet_id")
    val petId: Long,
    @ColumnInfo(name="note_id")
    val noteId: Long
)