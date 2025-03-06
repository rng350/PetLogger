package com.hfad.petlogger.weights.data

import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetTimeDisplayUseCase
import com.hfad.petlogger.common.util.GetWeightGramsDisplayUseCase
import java.time.OffsetDateTime

data class WeightForSelectionFetched(
    override val weightId: Long,
    override val weightGramsAmt: Int,
    override val weightDateTime: OffsetDateTime,
    override val weightPetName: String
): FetchedWeight, WithPetName {
    fun toWeightForSelection(): WeightForSelection {
        val dateDisplay = GetDateDisplayUseCase()
        val timeDisplay = GetTimeDisplayUseCase()
        val weightGramsDisplayUseCase = GetWeightGramsDisplayUseCase()
        return WeightForSelection(
            weightId = weightId,
            weightGramsAmt = weightGramsDisplayUseCase(weightGramsAmt),
            weightDate = dateDisplay(weightDateTime),
            weightTime = timeDisplay(weightDateTime),
            weightPetName = weightPetName
        )
    }
}
