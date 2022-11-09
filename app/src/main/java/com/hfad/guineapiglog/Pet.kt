package com.hfad.guineapiglog

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(tableName = "pet_table")
data class Pet(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="pet_id")
    var petID : Long = 0L,

    @ColumnInfo(name="pet_name")
    var petName : String = "",

    @ColumnInfo(name="pet_species")
    var petSpecies : String = "N/A",

    @ColumnInfo(name="pet_breed")
    var petBreed : String = "N/A",

    @ColumnInfo(name="pet_sex")
    var petSex : String = "N/A",

    @ColumnInfo(name="pet_dob")
    var petDOB : OffsetDateTime = OffsetDateTime.MIN,

    @ColumnInfo(name="has_dob")
    var hasDOB : Boolean = false
)
