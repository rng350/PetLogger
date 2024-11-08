package com.hfad.petlogger.tags.usecases

import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.Tag

class GetTagsOfPetUseCase(private val petRepository: PetRepository, private val petId: Long):
    GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return petRepository.getTagsOfPet(petId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}