package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetDao
import com.hfad.petlogger.pets.PetWithProfilePic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSinglePetUseCase(private val petDao: PetDao, private val petId: Long): GetItemsUseCase<PetWithProfilePic> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        listOf(petDao.getPetWithProfilePic(petId))
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}