package com.hfad.petlogger.pets

import android.net.Uri
import java.time.LocalDate

data class PetDetails(
    val petId : Long,
    val petName : String,
    val petDOB : LocalDate?,
    val petSpecies : String,
    val petBreed : String,
    val petSex : String,
    val petProfilePicUri: Uri?
)