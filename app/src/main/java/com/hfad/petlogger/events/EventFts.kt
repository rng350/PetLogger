package com.hfad.petlogger.events

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import java.time.OffsetDateTime

@Fts4(contentEntity = Event::class)
@Entity(tableName = "event_table_fts")
data class EventFts(
    @ColumnInfo(name="event_id")
    val eventId : Long,

    @ColumnInfo(name="event_title")
    val title : String,

    @ColumnInfo(name="event_details")
    val details : String,

    @ColumnInfo(name="event_date")
    val date: OffsetDateTime
)