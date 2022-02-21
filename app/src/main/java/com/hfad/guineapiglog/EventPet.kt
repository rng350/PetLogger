package com.hfad.guineapiglog

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "event_pet_table", primaryKeys = ["event_id", "pet_id"])
data class EventPet(
    @ColumnInfo(name="event_id")
    var eventId: Long = 0L,

    @ColumnInfo(name="pet_id")
    var petId: Long = 0L
)