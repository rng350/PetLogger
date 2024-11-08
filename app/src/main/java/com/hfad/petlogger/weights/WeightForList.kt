package com.hfad.petlogger.weights

import android.net.Uri
import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetTimeDisplayUseCase

data class WeightForList(
    val weightId: Long,
    val weightGramsAmt: String,
    val weightDate: String,
    val weightTime: String,
    val weightPetName: String,
    val prevWeightDifference: String
)