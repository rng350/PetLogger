package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.events.EventRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetWithProfilePic

class GetMorePetsOfEventUseCase(
    private val eventRepository: EventRepository,
    private val eventId: Long,
    private val petsAmt: Int
): GetPaginatedPetsUseCase(petsAmt) {
    override suspend fun fetchPets(lastPetId: Long): List<PetWithProfilePic> {
        return eventRepository.getPetsOfEventPaginated(eventId, lastPetId, petsAmt)
    }
}