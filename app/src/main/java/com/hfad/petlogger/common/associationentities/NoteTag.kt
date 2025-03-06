package com.hfad.petlogger.common.associationentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.hfad.petlogger.common.util.Constants.Companion.noteTagTableHeader
import com.hfad.petlogger.common.util.Constants.Companion.tagIdField
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.tags.data.Tag

@Entity(
    tableName = noteTagTableHeader,
    primaryKeys = ["note_id", tagIdField],
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["note_id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = [tagIdField],
            childColumns = [tagIdField],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class NoteTag(
    @ColumnInfo(name = "note_id", index = true)
    var noteId: Long,
    @ColumnInfo(name = tagIdField, index = true)
    var tagId: Long
)
