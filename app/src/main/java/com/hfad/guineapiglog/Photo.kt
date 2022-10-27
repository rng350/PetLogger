package com.hfad.guineapiglog

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Entity(tableName = "photo_table",
        primaryKeys = ["photo_id"])
data class Photo (
    @ColumnInfo(name="photo_id")
    val id: Long,

    @ColumnInfo(name="photo_name")
    val name: String,

    @ColumnInfo(name="photo_uri")
    val contentUri: Uri,

    @ColumnInfo(name="photo_width")
    val width: Int,

    @ColumnInfo(name="photo_height")
    val height: Int,

    @ColumnInfo(name="photo_notes")
    val notes: String
)