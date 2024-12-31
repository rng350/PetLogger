package com.hfad.petlogger.pets

import java.time.LocalDate

data class PetDetailsForEdit(
    val petId : Long,
    val petName : String,
    val petDateOfBirth : LocalDate?,
    val petDateOfPassing : LocalDate?,
    val petSpecies : String,
    val petBreed : String,
    val petSex : String
)