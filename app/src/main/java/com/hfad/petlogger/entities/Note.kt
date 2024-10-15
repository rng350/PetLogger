package com.hfad.petlogger.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(
    tableName = "note_table",
    indices = [
        Index("note_last_updated", "note_id")
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="note_id")
    var id: Long = 0L,

    @ColumnInfo(name="note_title")
    var title: String = "",

    @ColumnInfo(name="note_details")
    var details: String = "",

    @ColumnInfo(name="note_last_updated")
    var lastUpdated: OffsetDateTime = OffsetDateTime.now()
)
