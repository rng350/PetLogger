package com.hfad.petlogger.tags.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.domain.PetRepository
import com.hfad.petlogger.tags.data.Tag

class GetAllTagsOfPetAlphabeticalOrderUseCase(private val petRepository: PetRepository, private val petId: Long):
    GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return petRepository.getTagsOfPetAlphabeticalOrder(petId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}