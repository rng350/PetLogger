package com.hfad.petlogger.pets.domain.usecases

import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.pets.data.PetDao
import com.hfad.petlogger.pets.data.PetDetailsForDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetPetDetailsForDisplayUseCase(private val petDao: PetDao, private val petId: Long): GetSingleItemUseCase<GetPetDetailsForDisplayUseCase.Result> {
    override suspend fun invoke(): Result = withContext(Dispatchers.IO) {
        val fetchedPetDetails = petDao.getPetDetailsForDisplay(petId)
        return@withContext if (fetchedPetDetails == null) Result.Failure
            else Result.Success(fetchedPetDetails.toPetDetailsForDisplay())
    }
    sealed class Result {
        data class Success(val fetchedPet: PetDetailsForDisplay): Result()
        data object Failure: Result()
    }
}