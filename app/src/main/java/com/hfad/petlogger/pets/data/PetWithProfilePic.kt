package com.hfad.petlogger.pets.data

import android.net.Uri

data class PetWithProfilePic(
    val petName: String,
    val petId: Long,
    val petProfilePicUri: Uri?
)