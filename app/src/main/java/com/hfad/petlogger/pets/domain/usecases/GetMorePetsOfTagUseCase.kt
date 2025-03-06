package com.hfad.petlogger.pets.domain.usecases

import com.hfad.petlogger.pets.data.PetDao
import com.hfad.petlogger.pets.data.PetWithProfilePic

class GetMorePetsOfTagUseCase(
    private val petDao: PetDao,
    private val tagId: Long,
    private val petsAmt: Int
): GetPaginatedPetsUseCase(petsAmt) {
    override suspend fun fetchPets(lastPetId: Long): List<PetWithProfilePic>
        = petDao.getAllPetsOfTagPaginated(tagId, lastPetId, petsAmt)
}