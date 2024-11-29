package com.hfad.petlogger.weights

import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetTimeDisplayUseCase
import com.hfad.petlogger.common.util.GetWeightGramsDisplayUseCase
import java.time.OffsetDateTime

data class PetWeightForSelectionFetched(
    override val weightId: Long,
    override val weightGramsAmt: Int,
    override val weightDateTime: OffsetDateTime
): FetchedWeight {
    fun toPetWeightForSelection(): PetWeightForSelection {
        val dateDisplay = GetDateDisplayUseCase()
        val timeDisplay = GetTimeDisplayUseCase()
        val weightGramsDisplay = GetWeightGramsDisplayUseCase()
        return PetWeightForSelection(
            weightId = weightId,
            weightAmt = weightGramsDisplay(weightGramsAmt),
            weightDate = dateDisplay(weightDateTime),
            weightTime = timeDisplay(weightDateTime)
        )
    }
}