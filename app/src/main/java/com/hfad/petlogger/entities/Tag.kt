package com.hfad.petlogger.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_table")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tag_id", index=true)
    var tagId: Long = 0L,
    @ColumnInfo(name = "tag_name", index = true)
    var tagName: String
)