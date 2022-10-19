package com.hfad.guineapiglog

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName="photo_event_table",
    primaryKeys=["photo_id", "event_id"],
    foreignKeys = arrayOf(
        ForeignKey(entity = Photo::class,
            parentColumns = arrayOf("photo_id"),
            childColumns = arrayOf("photo_id"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = Event::class,
            parentColumns = arrayOf("event_id"),
            childColumns = arrayOf("event_id"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE)
    )
)
data class PhotoEvent(
    @ColumnInfo(name="photo_id")
    val photoID: Long,

    @ColumnInfo(name="event_id")
    val eventID: Long
)
