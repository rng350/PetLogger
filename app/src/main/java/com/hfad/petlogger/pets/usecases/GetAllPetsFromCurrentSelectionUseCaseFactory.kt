package com.hfad.petlogger.pets.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.factories.GetAllCurrentSelectionUseCaseFactory
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetWithProfilePic

class GetAllPetsFromCurrentSelectionUseCaseFactory:
    GetAllCurrentSelectionUseCaseFactory<PetWithProfilePic> {
    override fun createGetAllCurrentSelectionUseCase(currentSelection: LiveData<List<PetWithProfilePic>>): GetItemsUseCase<PetWithProfilePic> {
        return GetAllPetsFromCurrentSelectionUseCase(currentSelection)
    }
}