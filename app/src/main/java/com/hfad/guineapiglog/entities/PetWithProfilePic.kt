package com.hfad.guineapiglog.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.hfad.guineapiglog.entities.Pet
import com.hfad.guineapiglog.entities.PetProfilePhoto
import com.hfad.guineapiglog.entities.Photo

data class PetWithProfilePic(
    @Embedded
    val pet: Pet,

    @Relation(
        parentColumn = "pet_id",
        entity = Photo::class,
        entityColumn = "photo_id",
        associateBy = Junction(
            value = PetProfilePhoto::class,
            parentColumn = "pet_id",
            entityColumn = "photo_id"
        )
    )
    val profilePic: Photo?
)