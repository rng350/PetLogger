package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.pets.PetDao
import com.hfad.petlogger.pets.PetDetailsForDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetPetDetailsUseCase(private val petDao: PetDao, private val petId: Long): GetSingleItemUseCase<GetPetDetailsUseCase.Result> {
    override suspend fun invoke(): Result = withContext(Dispatchers.IO) {
        val fetchedPetDetails = petDao.getPetDetails(petId)
        return@withContext if (fetchedPetDetails == null) Result.Failure
            else Result.Success(fetchedPetDetails.toPetDetailsForDisplay())
    }
    sealed class Result {
        data class Success(val fetchedPet: PetDetailsForDisplay): Result()
        data object Failure: Result()
    }
}