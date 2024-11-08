package com.hfad.petlogger.common.associationentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.photos.Photo

@Entity(
    tableName = "pet_photo_table",
    primaryKeys = ["pet_id", "photo_id"],
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = ["pet_id"],
            childColumns = ["pet_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Photo::class,
            parentColumns = ["photo_id"],
            childColumns = ["photo_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class PetPhoto(
    @ColumnInfo(name="pet_id", index=true)
    val petId: Long,
    @ColumnInfo(name="photo_id", index=true)
    val photoId: Long
)
