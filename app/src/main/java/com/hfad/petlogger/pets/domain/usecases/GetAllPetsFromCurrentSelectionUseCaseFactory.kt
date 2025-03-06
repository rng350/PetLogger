package com.hfad.petlogger.pets.domain.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.factories.GetAllCurrentSelectionUseCaseFactory
import com.hfad.petlogger.pets.data.PetWithProfilePic

class GetAllPetsFromCurrentSelectionUseCaseFactory:
    GetAllCurrentSelectionUseCaseFactory<PetWithProfilePic> {
    override fun createGetAllCurrentSelectionUseCase(currentSelection: LiveData<List<PetWithProfilePic>>): GetItemsUseCase<PetWithProfilePic> {
        return GetAllPetsFromCurrentSelectionUseCase(currentSelection)
    }
}