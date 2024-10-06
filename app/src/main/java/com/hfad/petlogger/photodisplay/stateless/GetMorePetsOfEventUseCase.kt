package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.NoteRepository

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
}