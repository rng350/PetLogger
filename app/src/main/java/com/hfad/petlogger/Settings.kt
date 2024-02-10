package com.hfad.petlogger

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings_table")
data class Settings(
    @PrimaryKey(autoGenerate = true)
    var settingsId : Long = 0L
)
