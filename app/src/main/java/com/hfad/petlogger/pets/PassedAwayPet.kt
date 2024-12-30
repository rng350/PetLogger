package com.hfad.petlogger.pets

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import java.time.LocalDate

@Entity(
    tableName = "passed_away_pet_table",
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = ["pet_id"],
            childColumns = ["pet_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("pet_id"),
        Index("pet_date_of_passing")
    ]
)
data class PassedAwayPet(
    @ColumnInfo(name="pet_id")
    @PrimaryKey(autoGenerate = true)
    val petId: Long,
    @ColumnInfo(name="pet_date_of_passing")
    val dateOfPassing: LocalDate
)