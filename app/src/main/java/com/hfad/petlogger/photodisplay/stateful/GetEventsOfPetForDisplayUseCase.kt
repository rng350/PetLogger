package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventForList
import com.hfad.petlogger.repositories.PetRepository
import kotlinx.coroutines.flow.Flow

class GetEventsOfPetForDisplayUseCase(private val petRepository: PetRepository, private val petId: Long): GetItemsForDisplayUseCase<EventForList> {
    override fun invoke(): Flow<List<EventForList>> {
        return petRepository.getPetEvents(petId)
    }
}