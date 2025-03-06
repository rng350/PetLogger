package com.hfad.petlogger.weights.data

data class PetWeightForDisplay(
    val weightId: Long,
    val weightGramsAmt: String,
    val weightDate: String,
    val weightTime: String,
    val prevWeightDifference: String
)