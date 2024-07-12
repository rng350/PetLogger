package com.hfad.petlogger.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

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
    ],
    indices = [Index("event_id"), Index("note_id")]
)
data class EventNote (
    @ColumnInfo(name="event_id")
    val eventId: Long,
    @ColumnInfo(name="note_id")
    val noteId: Long
)