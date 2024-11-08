package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.Event

class GetEventsOfPetUseCase(private val petRepository: PetRepository, private val petId: Long):
    GetItemsUseCase<Event> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<Event> {
        return petRepository.getPetEventsAsList(petId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}