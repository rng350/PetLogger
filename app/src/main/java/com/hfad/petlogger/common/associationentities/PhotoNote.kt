package com.hfad.petlogger.common.associationentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.photos.data.Photo

@Entity(
    tableName = "photo_note_table",
    primaryKeys = ["photo_id", "note_id"],
    foreignKeys = [
        ForeignKey(
            entity = Photo::class,
            parentColumns = ["photo_id"],
            childColumns = ["photo_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Note::class,
            parentColumns = ["note_id"],
            childColumns = ["note_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("photo_id","note_id"),
        Index("note_id","photo_id")
    ]
)
data class PhotoNote(
    @ColumnInfo(name = "photo_id")
    val photoId: Long,
    @ColumnInfo(name = "note_id")
    val noteId: Long
)