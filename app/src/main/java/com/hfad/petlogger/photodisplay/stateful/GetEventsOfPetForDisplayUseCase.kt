package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.repositories.PetRepository
import kotlinx.coroutines.flow.Flow

class GetEventsOfPetForDisplayUseCase(private val petRepository: PetRepository, private val petId: Long): GetAssociatedItemsForDisplayUseCase<Event> {
    override fun invoke(): Flow<List<Event>> {
        return petRepository.getPetEvents(petId)
    }
}