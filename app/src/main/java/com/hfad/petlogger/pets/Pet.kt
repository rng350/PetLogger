package com.hfad.petlogger.pets

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Entity(tableName = "pet_table")
@Parcelize
data class Pet(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="pet_id")
    var petID : Long = 0L,

    @ColumnInfo(name="pet_name")
    var petName : String = "",

    @ColumnInfo(name="pet_species")
    var petSpecies : String = "",

    @ColumnInfo(name="pet_breed")
    var petBreed : String = "",

    @ColumnInfo(name="pet_sex")
    var petSex : String = "",

    @ColumnInfo(name="pet_dob")
    var petDOB : LocalDate? = null
) : Parcelable