package com.hfad.petlogger.entities

import android.net.Uri
import com.hfad.petlogger.util.GetDateDisplayUseCase
import com.hfad.petlogger.util.GetTimeDisplayUseCase

data class WeightForList(
    val weightId: Long,
    val weightGramsAmt: String,
    val weightDate: String,
    val weightTime: String,
    val weightPetName: String,
    val weightPetPhotoUri: Uri?,
    val prevWeightDifference: String
)