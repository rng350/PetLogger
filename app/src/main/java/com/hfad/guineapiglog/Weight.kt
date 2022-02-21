package com.hfad.guineapiglog

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(tableName = "weight_table")
data class Weight(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="weight_id")
    var weightId: Long = 0L,

    @ColumnInfo(name="weight_pet_id")
    var petId: Long = 0L,

    @ColumnInfo(name="weight_grams")
    var weightGrams: Long = 0L/*,

    @ColumnInfo(name="weight_datetime")
    val weightDateTime : OffsetDateTime = OffsetDateTime.MIN,

    @ColumnInfo(name="weight_notes")
    val weightNotes : String = ""*/
)