package com.hfad.guineapiglog

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "event_pet_table",
    primaryKeys = ["event_id", "pet_id"],
    foreignKeys = arrayOf(
        ForeignKey(entity = Event::class,
            parentColumns = arrayOf("event_id"),
            childColumns = arrayOf("event_id"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = Pet::class,
            parentColumns = arrayOf("pet_id"),
            childColumns = arrayOf("pet_id"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE)
    )
)
data class EventPet(
    @ColumnInfo(name="event_id")
    var eventId: Long = 0L,

    @ColumnInfo(name="pet_id")
    var petId: Long = 0L
)