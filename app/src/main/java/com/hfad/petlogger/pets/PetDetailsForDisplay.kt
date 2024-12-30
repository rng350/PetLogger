package com.hfad.petlogger.pets

import android.net.Uri

data class PetDetailsForDisplay(
    val petId : Long = -1L,
    val petName : String = "",
    val petDateOfBirth : String = "",
    val petDateOfPassing : String = "",
    val petAgeDisplay : String = "",
    val petSpecies : String = "",
    val petBreed : String = "",
    val petSex : String = "",
    val latestPetWeightAmt: String = "",
    val latestPetWeightHowLongAgo: String = "",
    val petProfilePicUri: Uri? = null
)