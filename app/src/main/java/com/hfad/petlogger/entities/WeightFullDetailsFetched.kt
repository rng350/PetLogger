package com.hfad.petlogger.entities

import android.net.Uri
import com.hfad.petlogger.util.GetDateDisplayUseCase
import com.hfad.petlogger.util.GetTimeDisplayUseCase
import java.time.OffsetDateTime

data class WeightFullDetailsFetched(
    val curWeightId: Long,
    val curWeightGrams: Int,
    val curWeightDateTime : OffsetDateTime,
    val curWeightNotes : String,
    val prevWeightId: Long?,
    val prevWeightGrams: Int?,
    val prevWeightDateTime: OffsetDateTime?,
    val petId: Long,
    val petName: String,
    val petProfilePicUri: Uri?
) {
    fun toState(): WeightFullDetailsState {
        return WeightFullDetailsState.fromWeightFullDetailsFetched(this)
    }
}