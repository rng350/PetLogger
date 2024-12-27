package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.events.EventForList
import java.time.OffsetDateTime

class GetMoreEventsOfPetUseCase(
    private val petRepository: PetRepository,
    private val petId: Long,
    private val eventAmt: Int
): GetPaginatedEventsUseCase(eventAmt) {
    override suspend fun fetchEvents(lastEventDate: OffsetDateTime, lastEventId: Long): List<Event>
        = petRepository.getPetEventsAsListPaginated(petId, lastEventDate, lastEventId, eventAmt)
}