package com.hfad.petlogger.entities

import android.net.Uri
import com.hfad.petlogger.util.GetDateDisplayUseCase
import com.hfad.petlogger.util.GetTimeDisplayUseCase
import java.time.OffsetDateTime

data class WeightForListFetched(
    val weightId: Long,
    val weightGramsAmt: Int,
    val weightDateTime: OffsetDateTime,
    val weightPetName: String,
    val weightPetPhotoUri: Uri?,
    val prevWeightGramsAmt: Int?
) {
        fun toWeightForList(): WeightForList {
            val dateDisplay = GetDateDisplayUseCase()
            val timeDisplay = GetTimeDisplayUseCase()
            val weightDiffAmt = weightGramsAmt - (prevWeightGramsAmt ?: 0)
            val prevWeightDifferenceDisplay: String =
                if (prevWeightGramsAmt!=null)
                    "${if (weightDiffAmt>0) "+" else ""}${weightDiffAmt}g"
                else "---"
            return WeightForList(
                weightId = weightId,
                weightGramsAmt = "${weightGramsAmt}g",
                weightDate = dateDisplay(weightDateTime),
                weightTime = timeDisplay(weightDateTime),
                weightPetName = weightPetName,
                weightPetPhotoUri = weightPetPhotoUri,
                prevWeightDifference = prevWeightDifferenceDisplay
            )
    }
}