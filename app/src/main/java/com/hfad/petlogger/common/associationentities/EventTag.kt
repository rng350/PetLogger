package com.hfad.petlogger.common.associationentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.hfad.petlogger.events.data.Event
import com.hfad.petlogger.tags.data.Tag

@Entity(
    tableName = "event_tag_table",
    primaryKeys = ["event_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["event_id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["tag_id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class EventTag(
    @ColumnInfo(name = "event_id", index = true)
    var eventId: Long,
    @ColumnInfo(name = "tag_id", index = true)
    var tagId: Long
)
