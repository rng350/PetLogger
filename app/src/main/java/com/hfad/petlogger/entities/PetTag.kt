package com.hfad.petlogger.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "pet_tag_table",
    primaryKeys = ["pet_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = ["pet_id"],
            childColumns = ["pet_id"],
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
data class PetTag(
    @ColumnInfo(name = "pet_id", index = true)
    var petId: Long,
    @ColumnInfo(name = "tag_id", index = true)
    var tagId: Long
)
