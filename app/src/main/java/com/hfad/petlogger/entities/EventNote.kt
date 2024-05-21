package com.hfad.petlogger.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "event_note_table",
    primaryKeys = ["event_id", "note_id"],
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = arrayOf("event_id"),
            childColumns = arrayOf("event_id"),
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
data class EventNote (
    @ColumnInfo(name="event_id")
    val eventtId: Long,
    @ColumnInfo(name="note_id")
    val noteId: Long
)