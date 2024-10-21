package com.hfad.petlogger.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = Note::class)
@Entity(tableName = "note_table_fts")
class NoteFts (
    @ColumnInfo(name="note_id")
    val id: Long,

    @ColumnInfo(name="note_title")
    val title: String,

    @ColumnInfo(name="note_details")
    val details: String
)