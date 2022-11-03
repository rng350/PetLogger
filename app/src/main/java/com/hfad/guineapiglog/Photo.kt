package com.hfad.guineapiglog

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "photo_table")
data class Photo (
    @ColumnInfo(name="photo_id")
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name="photo_name")
    val name: String,

    @ColumnInfo(name="photo_uri")
    val contentUri: Uri,

    @ColumnInfo(name="photo_width")
    val width: Int,

    @ColumnInfo(name="photo_height")
    val height: Int,

    @ColumnInfo(name="photo_filesize")
    val size: Double,

    @ColumnInfo(name="photo_date")
    val date: LocalDateTime?
)