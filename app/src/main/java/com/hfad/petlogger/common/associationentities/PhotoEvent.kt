package com.hfad.petlogger.common.associationentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.photos.Photo

@Entity(
    tableName="photo_event_table",
    primaryKeys=["photo_id", "event_id"],
    foreignKeys = [
        ForeignKey(
            entity = Photo::class,
            parentColumns = arrayOf("photo_id"),
            childColumns = arrayOf("photo_id"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
        ForeignKey(
            entity = Event::class,
            parentColumns = arrayOf("event_id"),
            childColumns = arrayOf("event_id"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("event_id")
    ]
)
data class PhotoEvent(
    @ColumnInfo(name="photo_id")
    val photoID: Long,

    @ColumnInfo(name="event_id")
    val eventID: Long
)
