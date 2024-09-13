package com.hfad.petlogger.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "photo_tag_table",
    primaryKeys = ["photo_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = Photo::class,
            parentColumns = ["photo_id"],
            childColumns = ["photo_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["tag_id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class PhotoTag(
    @ColumnInfo(name = "photo_id", index = true)
    var photoId: Long,
    @ColumnInfo(name = "tag_id", index = true)
    var tagId: Long
)
