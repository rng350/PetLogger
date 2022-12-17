package com.hfad.guineapiglog.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(
    tableName="event_table",
    indices = [
        Index("event_date")
    ]
)
data class Event(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="event_id")
    var eventId : Long = 0L,

    @ColumnInfo(name="event_title")
    var title : String = "",

    @ColumnInfo(name="event_details")
    var details : String = "",

    @ColumnInfo(name="event_date")
    var date: OffsetDateTime = OffsetDateTime.now()
)
