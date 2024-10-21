package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetPetsOfEventUseCase(private val eventRepository: EventRepository, private val eventId: Long): GetItemsUseCase<PetWithProfilePic> {
    override val onLastPage: Boolean = false
    override suspend operator fun invoke(): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        eventRepository.getPetsOfEvent(eventId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}