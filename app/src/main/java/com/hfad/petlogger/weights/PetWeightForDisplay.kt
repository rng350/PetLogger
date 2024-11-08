package com.hfad.petlogger.weights

import com.hfad.petlogger.weights.Weight

data class PetWeightForDisplay(
    val weight: Weight,
    val weightDate: String,
    val weightTime: String,
    val weightAmt: String
)