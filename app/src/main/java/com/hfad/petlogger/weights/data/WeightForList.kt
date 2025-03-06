package com.hfad.petlogger.weights.data

data class WeightForList(
    val weightId: Long,
    val weightGramsAmt: String,
    val weightDate: String,
    val weightTime: String,
    val weightPetName: String,
    val prevWeightDifference: String
)