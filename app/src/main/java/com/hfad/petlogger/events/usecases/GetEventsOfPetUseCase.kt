package com.hfad.petlogger.events.usecases

import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.EventForList

class GetEventsOfPetUseCase(private val petRepository: PetRepository, private val petId: Long):
    GetItemsUseCase<EventForList> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<EventForList> {
        return petRepository.getPetEventsAsList(petId).map{it.toEventForList()}
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}