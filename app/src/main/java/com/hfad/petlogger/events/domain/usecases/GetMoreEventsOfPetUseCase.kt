package com.hfad.petlogger.events.domain.usecases

import com.hfad.petlogger.events.data.Event
import com.hfad.petlogger.pets.domain.PetRepository
import java.time.OffsetDateTime

class GetMoreEventsOfPetUseCase(
    private val petRepository: PetRepository,
    private val petId: Long,
    private val eventAmt: Int
): GetPaginatedEventsUseCase(eventAmt) {
    override suspend fun fetchEvents(lastEventDate: OffsetDateTime, lastEventId: Long): List<Event>
        = petRepository.getPetEventsAsListPaginated(petId, lastEventDate, lastEventId, eventAmt)
}