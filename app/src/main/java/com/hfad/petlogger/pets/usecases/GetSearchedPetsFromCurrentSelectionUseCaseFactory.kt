package com.hfad.petlogger.pets.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.factories.GetSearchedCurrentSelectionUseCaseFactory
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.pets.PetDao
import com.hfad.petlogger.pets.PetWithProfilePic

class GetSearchedPetsFromCurrentSelectionUseCaseFactory(private val petDao: PetDao):
    GetSearchedCurrentSelectionUseCaseFactory<PetWithProfilePic> {
    override fun createGetSearchedCurrentSelectionUseCase(currentSelection: LiveData<List<PetWithProfilePic>>): GetSearchedItemsUseCase<PetWithProfilePic> {
        return GetSearchedPetsFromCurrentSelectionUseCase(petDao, currentSelection)
    }
}