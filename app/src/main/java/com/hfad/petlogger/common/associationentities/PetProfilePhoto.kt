package com.hfad.petlogger.common.associationentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName="pet_profile_photo_table",
    foreignKeys = [
        ForeignKey(entity = PetPhoto::class,
        parentColumns = ["pet_id", "photo_id"],
        childColumns = ["pet_id", "photo_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE)
    ],
    indices = [Index("photo_id")]
)
data class PetProfilePhoto(
    @ColumnInfo(name="photo_id")
    val photoID: Long,

    @ColumnInfo(name="pet_id")
    @PrimaryKey
    val petID: Long
)