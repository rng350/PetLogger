package com.hfad.petlogger.weights.data

import android.net.Uri
import com.hfad.petlogger.common.util.GetDateTimeDisplayUseCase
import com.hfad.petlogger.common.util.GetPeriodDisplayUseCase
import com.hfad.petlogger.common.util.GetTimeDifferenceUseCase

data class WeightFullDetailsState(
    val curWeight: CurrentWeight,
    val prevWeight: PreviousWeight?,
    val weightPet: WeightPet
) {
    data class CurrentWeight(
        val id: Long,
        val weightGrams: Int,
        val weightDateTimeDisplay: String,
        val notes: String
    )
    data class PreviousWeight(
        val id: Long,
        val weightGrams: Int,
        val weightDateTimeDisplay: String,
        val weighingPeriodElapsed: String,
        val weighingTimeDifference: String,
        val weightDifferenceGrams: Int
    )

    data class WeightPet(
        val petId: Long,
        val petName: String,
        val petProfilePicUri: Uri?
    )

    companion object {
        fun fromWeightFullDetailsFetched(weightFullDetailsFetched: WeightFullDetailsFetched): WeightFullDetailsState {
            val getDateTimeDisplay = GetDateTimeDisplayUseCase()
            val getPeriodDisplay = GetPeriodDisplayUseCase()
            val getTimeDifference = GetTimeDifferenceUseCase()

            val curWeight = CurrentWeight(
                id = weightFullDetailsFetched.curWeightId,
                weightGrams = weightFullDetailsFetched.curWeightGrams,
                weightDateTimeDisplay = getDateTimeDisplay(weightFullDetailsFetched.curWeightDateTime),
                notes = weightFullDetailsFetched.curWeightNotes
            )
            val prevWeight =
                if (weightFullDetailsFetched.prevWeightId != null && weightFullDetailsFetched.prevWeightGrams != null && weightFullDetailsFetched.prevWeightDateTime != null)
                    PreviousWeight(
                        id = weightFullDetailsFetched.prevWeightId,
                        weightGrams = weightFullDetailsFetched.prevWeightGrams,
                        weightDateTimeDisplay = getDateTimeDisplay(weightFullDetailsFetched.prevWeightDateTime),
                        weighingPeriodElapsed = getPeriodDisplay(weightFullDetailsFetched.prevWeightDateTime, weightFullDetailsFetched.curWeightDateTime),
                        weighingTimeDifference = getTimeDifference(weightFullDetailsFetched.prevWeightDateTime, weightFullDetailsFetched.curWeightDateTime),
                        weightDifferenceGrams = weightFullDetailsFetched.curWeightGrams - weightFullDetailsFetched.prevWeightGrams
                    )
                else
                    null
            val weightPet = WeightPet(
                petId = weightFullDetailsFetched.petId,
                petName = weightFullDetailsFetched.petName,
                petProfilePicUri = weightFullDetailsFetched.petProfilePicUri
            )
            return WeightFullDetailsState(curWeight, prevWeight, weightPet)
        }
    }
}
