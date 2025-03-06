package com.hfad.petlogger.pets.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.pets.data.PetWithProfilePic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetPetsOfEventUseCase(private val eventRepository: EventRepository, private val eventId: Long):
    GetItemsUseCase<PetWithProfilePic> {
    override val onLastPage: Boolean = false
    override suspend operator fun invoke(): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        eventRepository.getPetsOfEvent(eventId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}