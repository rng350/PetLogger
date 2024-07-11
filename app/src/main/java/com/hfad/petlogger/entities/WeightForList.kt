package com.hfad.petlogger.entities

import android.net.Uri

data class WeightForList(
    val weightId: Long,
    val weightGramsAmt: String,
    val weightDate: String,
    val weightTime: String,
    val weightPetName: String,
    val weightPetPhotoUri: Uri?
)
