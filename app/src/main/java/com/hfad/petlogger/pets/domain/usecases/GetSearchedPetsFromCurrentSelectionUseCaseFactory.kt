package com.hfad.petlogger.pets.domain.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.usecases.factories.GetSearchedCurrentSelectionUseCaseFactory
import com.hfad.petlogger.pets.data.PetDao
import com.hfad.petlogger.pets.data.PetWithProfilePic

class GetSearchedPetsFromCurrentSelectionUseCaseFactory(private val petDao: PetDao):
    GetSearchedCurrentSelectionUseCaseFactory<PetWithProfilePic> {
    override fun createGetSearchedCurrentSelectionUseCase(currentSelection: LiveData<List<PetWithProfilePic>>): GetSearchedItemsUseCase<PetWithProfilePic> {
        return GetSearchedPetsFromCurrentSelectionUseCase(petDao, currentSelection)
    }
}