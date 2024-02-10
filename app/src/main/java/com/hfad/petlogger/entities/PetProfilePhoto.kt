package com.hfad.petlogger.entities

import androidx.room.*

@Entity(tableName="pet_profile_photo_table",
    foreignKeys = [
        ForeignKey(entity = Photo::class,
        parentColumns = arrayOf("photo_id"),
        childColumns = arrayOf("photo_id"),
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE),

        ForeignKey(entity = Pet::class,
        parentColumns = arrayOf("pet_id"),
        childColumns = arrayOf("pet_id"),
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE)
    ],
    indices = [Index("photo_id")]
)
data class PetProfilePhoto(
    @ColumnInfo(name="photo_id")
    val photoID: Long,

    @ColumnInfo(name="pet_id")
    @PrimaryKey()
    val petID: Long
)