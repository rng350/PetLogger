package com.hfad.petlogger.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "weight_note_table",
    primaryKeys = ["weight_id", "note_id"],
    foreignKeys = [
        ForeignKey(
            entity = Weight::class,
            parentColumns = arrayOf("weight_id"),
            childColumns = arrayOf("weight_id"),
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
data class WeightNote (
    @ColumnInfo(name="weight_id")
    val weightId: Long,
    @ColumnInfo(name="note_id")
    val noteId: Long
)