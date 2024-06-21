package com.hfad.petlogger.entities

data class PetWeightForDisplay(
    val weight: Weight,
    val weightDate: String,
    val weightTime: String,
    val weightAmt: String
)