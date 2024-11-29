package com.hfad.petlogger.weights

import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetTimeDisplayUseCase
import com.hfad.petlogger.common.util.GetWeightDifferenceDisplayUseCase
import com.hfad.petlogger.common.util.GetWeightGramsDisplayUseCase
import java.time.OffsetDateTime

data class WeightForListFetched(
    override val weightId: Long,
    override val weightGramsAmt: Int,
    override val weightDateTime: OffsetDateTime,
    override val weightPetName: String,
    override val prevWeightGramsAmt: Int?
): FetchedWeight, WithPreviousWeight, WithPetName {
        fun toWeightForList(): WeightForList {
            val dateDisplay = GetDateDisplayUseCase()
            val timeDisplay = GetTimeDisplayUseCase()
            val weightGramsDisplayUseCase = GetWeightGramsDisplayUseCase()
            val weightDifferenceDisplay = GetWeightDifferenceDisplayUseCase()
            return WeightForList(
                weightId = weightId,
                weightGramsAmt = weightGramsDisplayUseCase(weightGramsAmt),
                weightDate = dateDisplay(weightDateTime),
                weightTime = timeDisplay(weightDateTime),
                weightPetName = weightPetName,
                prevWeightDifference = weightDifferenceDisplay(
                    curWeightGramsAmt = weightGramsAmt,
                    prevWeightGramsAmt = prevWeightGramsAmt
                )
            )
    }
}