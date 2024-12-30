package com.hfad.petlogger.pets

import android.net.Uri
import android.util.Log
import com.hfad.petlogger.common.util.DisplayTimeAgoUseCase
import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetPeriodDisplayUseCase
import com.hfad.petlogger.common.util.GetWeightGramsDisplayUseCase
import java.time.LocalDate
import java.time.OffsetDateTime

data class PetDetailsFetched(
    val petId : Long,
    val petName : String,
    val petDateOfBirth : LocalDate?,
    val petDateOfPassing : LocalDate?,
    val petSpecies : String,
    val petBreed : String,
    val petSex : String,
    val latestPetWeightGrams: Int?,
    val latestPetWeightDateTime: OffsetDateTime?,
    val petProfilePicUri: Uri?
) {
    fun toPetDetailsForDisplay(): PetDetailsForDisplay {
        return PetDetailsForDisplay(
            petId = petId,
            petName = petName,
            petDateOfBirth = GetDateDisplayUseCase().invoke(petDateOfBirth, monthInFull = true),
            petDateOfPassing = if (petDateOfPassing!=null) GetDateDisplayUseCase().invoke(petDateOfPassing, monthInFull = true) else "",
            petAgeDisplay =
                if (this.petDateOfBirth != null)
                    GetPeriodDisplayUseCase().invoke(
                        startDate = this.petDateOfBirth,
                        endDate = this.petDateOfPassing
                    )
                else "N/A",
            petSpecies = petSpecies,
            petBreed = petBreed,
            petSex = petSex,
            petProfilePicUri = petProfilePicUri,
            latestPetWeightAmt = GetWeightGramsDisplayUseCase().invoke(latestPetWeightGrams),
            latestPetWeightHowLongAgo = DisplayTimeAgoUseCase().invoke(latestPetWeightDateTime)
        )
    }
}