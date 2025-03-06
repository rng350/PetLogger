package com.hfad.petlogger.pets.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.data.PetWithProfilePic
import com.hfad.petlogger.pets.domain.PetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAllPetsWithProfilePhotosUseCase(private val petRepository: PetRepository):
    GetItemsUseCase<PetWithProfilePic> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        petRepository.getAllPets()
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}