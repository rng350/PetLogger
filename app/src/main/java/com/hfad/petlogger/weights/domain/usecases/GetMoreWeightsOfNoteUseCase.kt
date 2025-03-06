package com.hfad.petlogger.weights.domain.usecases

import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.weights.data.WeightForListFetched
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