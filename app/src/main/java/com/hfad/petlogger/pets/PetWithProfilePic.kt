package com.hfad.petlogger.pets

import android.net.Uri

data class PetWithProfilePic(
    val petName: String,
    val petId: Long,
    val petProfilePicUri: Uri?
)