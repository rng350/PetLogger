package com.hfad.guineapiglog

import android.net.Uri

data class PetNameWithPhoto(
    val name: String,
    val photoUri: Uri?
)