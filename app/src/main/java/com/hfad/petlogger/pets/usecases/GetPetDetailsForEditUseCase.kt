package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.pets.PetDao
import com.hfad.petlogger.pets.PetDetailsForEdit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetPetDetailsForEditUseCase(private val petDao: PetDao, private val petId: Long): GetSingleItemUseCase<GetPetDetailsForEditUseCase.Result> {
    override suspend fun invoke(): Result = withContext(Dispatchers.IO) {
        val fetchedPetDetails = petDao.getPetDetailsForEdit(petId)
        return@withContext if (fetchedPetDetails == null) Result.Failure
        else Result.Success(fetchedPetDetails)
    }

    sealed class Result {
        data class Success(val fetchedPet: PetDetailsForEdit): Result()
        data object Failure: Result()
    }
}