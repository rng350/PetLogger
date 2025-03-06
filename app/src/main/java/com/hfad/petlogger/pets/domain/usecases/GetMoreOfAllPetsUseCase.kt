package com.hfad.petlogger.pets.domain.usecases

import com.hfad.petlogger.pets.data.PetWithProfilePic
import com.hfad.petlogger.pets.domain.PetRepository

class GetMoreOfAllPetsUseCase(
    private val petRepository: PetRepository,
    private val petsAmt: Int
): GetPaginatedPetsUseCase(petsAmt) {
    override suspend fun fetchPets(lastPetId: Long): List<PetWithProfilePic> {
        return petRepository.getAllPetsPaginated(lastPetId, petsAmt)
    }
}