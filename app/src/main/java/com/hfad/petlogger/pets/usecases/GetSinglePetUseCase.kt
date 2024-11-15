package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.pets.PetDao
import com.hfad.petlogger.pets.PetWithProfilePic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSinglePetUseCase(private val petDao: PetDao, private val petId: Long): GetSingleItemUseCase<PetWithProfilePic> {
    override suspend fun invoke(): PetWithProfilePic = withContext(Dispatchers.IO) {
        petDao.getPetWithProfilePic(petId)
    }
}