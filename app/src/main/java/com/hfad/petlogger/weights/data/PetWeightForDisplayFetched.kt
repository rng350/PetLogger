package com.hfad.petlogger.weights.data

import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetTimeDisplayUseCase
import com.hfad.petlogger.common.util.GetWeightDifferenceDisplayUseCase
import com.hfad.petlogger.common.util.GetWeightGramsDisplayUseCase
import java.time.OffsetDateTime

data class PetWeightForDisplayFetched(
    override val weightId: Long,
    override val weightGramsAmt: Int,
    override val weightDateTime: OffsetDateTime,
    override val prevWeightGramsAmt: Int?
): FetchedWeight, WithPreviousWeight {
    fun toPetWeightForDisplay(): PetWeightForDisplay {
        val dateDisplay = GetDateDisplayUseCase()
        val timeDisplay = GetTimeDisplayUseCase()
        val weightGramsDisplayUseCase = GetWeightGramsDisplayUseCase()
        val weightDifferenceDisplay = GetWeightDifferenceDisplayUseCase()

        return PetWeightForDisplay(
            weightId = weightId,
            weightGramsAmt = weightGramsDisplayUseCase(weightGramsAmt),
            weightDate = dateDisplay(weightDateTime),
            weightTime = timeDisplay(weightDateTime),
            prevWeightDifference = weightDifferenceDisplay(
                curWeightGramsAmt = weightGramsAmt,
                prevWeightGramsAmt = prevWeightGramsAmt
            )
        )
    }
}