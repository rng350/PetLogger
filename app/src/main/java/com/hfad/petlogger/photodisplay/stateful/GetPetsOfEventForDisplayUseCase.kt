package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.EventRepository
import kotlinx.coroutines.flow.Flow

class GetPetsOfEventForDisplayUseCase(private val eventId: Long, private val eventRepository: EventRepository): GetItemsForDisplayUseCase<PetWithProfilePic> {
    override fun invoke(): Flow<List<PetWithProfilePic>> {
        return eventRepository.getPetsWithProfilePicOfEventAsFlow(eventId)
    }
}