package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.repositories.PetRepository

class GetEventsOfPetUseCase(private val petRepository: PetRepository, private val petId: Long): GetItemsUseCase<Event> {
    override suspend fun invoke(): List<Event> {
        return petRepository.getPetEventsAsList(petId)
    }
}