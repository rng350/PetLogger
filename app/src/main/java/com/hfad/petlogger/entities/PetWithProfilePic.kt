package com.hfad.petlogger.entities

import android.net.Uri

/*
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

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
)*/

data class PetWithProfilePic(
    val petName: String,
    val petId: Long,
    val petProfilePicUri: Uri?
)