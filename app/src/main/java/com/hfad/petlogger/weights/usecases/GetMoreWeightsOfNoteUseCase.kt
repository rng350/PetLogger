package com.hfad.petlogger.weights.usecases

import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.weights.WeightForList
import com.hfad.petlogger.weights.WeightForListFetched
import java.time.OffsetDateTime

class GetMoreWeightsOfNoteUseCase(
    private val noteRepository: NoteRepository,
    private val noteId: Long,
    private val weightsAmt: Int
): GetPaginatedWeightsForGeneralDisplay(weightsAmt) {
    override suspend fun fetchWeights(
        lastWeightDate: OffsetDateTime,
        lastWeightId: Long
    ): List<WeightForListFetched> {
        return noteRepository.getWeightsOfNotePaginated(noteId, lastWeightDate, lastWeightId, weightsAmt)
    }
}