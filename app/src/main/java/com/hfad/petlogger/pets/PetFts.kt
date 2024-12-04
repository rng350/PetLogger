package com.hfad.petlogger.pets

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import java.time.LocalDate

@Fts4(contentEntity = Pet::class)
@Entity(tableName = "pet_fts_table")
data class PetFts(
    @ColumnInfo(name="pet_id")
    var petID : Long,

    @ColumnInfo(name="pet_name")
    var petName : String ,

    @ColumnInfo(name="pet_species")
    var petSpecies : String,

    @ColumnInfo(name="pet_breed")
    var petBreed : String
)
