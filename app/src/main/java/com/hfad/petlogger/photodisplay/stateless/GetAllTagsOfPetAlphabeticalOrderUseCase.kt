package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.repositories.PetRepository

class GetAllTagsOfPetAlphabeticalOrderUseCase(private val petRepository: PetRepository, private val petId: Long): GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return petRepository.getTagsOfPetAlphabeticalOrder(petId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}