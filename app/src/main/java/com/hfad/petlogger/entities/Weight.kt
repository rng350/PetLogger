package com.hfad.petlogger.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hfad.petlogger.WithId
import java.time.OffsetDateTime

@Entity(
    tableName = "weight_table",
    indices = [
        Index(value = ["weight_pet_id", "weight_datetime"]),
        Index(value = ["weight_datetime"])
    ]
)
data class Weight(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="weight_id")
    override var id: Long = 0L,

    @ColumnInfo(name="weight_pet_id")
    var petId: Long = 0L,

    @ColumnInfo(name="weight_grams")
    var weightGrams: Int = 0,

    @ColumnInfo(name="weight_datetime")
    var weightDateTime : OffsetDateTime = OffsetDateTime.now(),

    @ColumnInfo(name="weight_notes")
    var weightNotes : String = ""
): WithId