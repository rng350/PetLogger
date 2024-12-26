package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetWithProfilePic

abstract class GetPaginatedPetsUseCase(private val petsAmt: Int): GetItemsUseCase<PetWithProfilePic> {
    private var lastPetId = Long.MIN_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<PetWithProfilePic> {
        val pets = fetchPets(lastPetId)
        lastPetId = pets.lastOrNull()?.petId ?: Long.MAX_VALUE
        _onLastPage = pets.size < petsAmt
        return pets
    }

    abstract suspend fun fetchPets(lastPetId: Long): List<PetWithProfilePic>

    override fun resetCurrentPoint() {
        lastPetId = Long.MIN_VALUE
        _onLastPage = false
    }
}