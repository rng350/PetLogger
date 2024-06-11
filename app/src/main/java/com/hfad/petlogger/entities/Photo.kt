package com.hfad.petlogger.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Entity(tableName = "photo_table")
data class Photo (
    @ColumnInfo(name="photo_id")
    @PrimaryKey(autoGenerate = false)
    var id: Long = 0L,

    @ColumnInfo(name="photo_title")
    var title: String = "",

    @ColumnInfo(name="photo_filename")
    var filename: String = "",

    @ColumnInfo(name="photo_uri")
    var contentUri: Uri = Uri.EMPTY,

    @ColumnInfo(name="photo_width")
    var width: Int = 0,

    @ColumnInfo(name="photo_height")
    var height: Int = 0,

    @ColumnInfo(name="photo_filesize")
    var size: Double = 0.0,

    @ColumnInfo(name="photo_date")
    var date: OffsetDateTime?
)