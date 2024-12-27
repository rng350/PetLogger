package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.pets.PetWithProfilePic

class GetMoreOfAllPetsUseCase(
    private val petRepository: PetRepository,
    private val petsAmt: Int
): GetPaginatedPetsUseCase(petsAmt) {
    override suspend fun fetchPets(lastPetId: Long): List<PetWithProfilePic> {
        return petRepository.getAllPetsPaginated(lastPetId, petsAmt)
    }
}