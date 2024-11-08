package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.events.EventRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetWithProfilePic

class GetMorePetsOfEventUseCase(
    private val eventRepository: EventRepository,
    private val eventId: Long,
    private val petsAmt: Int
): GetItemsUseCase<PetWithProfilePic> {
    private var lastPetId = Long.MIN_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    override suspend fun invoke(): List<PetWithProfilePic> {
        val pets = eventRepository.getPetsOfEventPaginated(eventId, lastPetId, petsAmt)
        lastPetId = pets.lastOrNull()?.petId ?: Long.MAX_VALUE
        _onLastPage = pets.size < petsAmt
        return pets
    }

    override fun resetCurrentPoint() {
        lastPetId = Long.MIN_VALUE
        _onLastPage = false
    }
}