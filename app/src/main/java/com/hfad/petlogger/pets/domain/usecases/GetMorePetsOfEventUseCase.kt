package com.hfad.petlogger.pets.domain.usecases

import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.pets.data.PetWithProfilePic

class GetMorePetsOfEventUseCase(
    private val eventRepository: EventRepository,
    private val eventId: Long,
    private val petsAmt: Int
): GetPaginatedPetsUseCase(petsAmt) {
    override suspend fun fetchPets(lastPetId: Long): List<PetWithProfilePic> {
        return eventRepository.getPetsOfEventPaginated(eventId, lastPetId, petsAmt)
    }
}