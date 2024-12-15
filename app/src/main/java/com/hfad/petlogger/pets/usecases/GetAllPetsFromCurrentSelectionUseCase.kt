package com.hfad.petlogger.pets.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetWithProfilePic

class GetAllPetsFromCurrentSelectionUseCase(
    val currentSelection: LiveData<List<PetWithProfilePic>>
): GetItemsUseCase<PetWithProfilePic> {
    private var _onLastPage = false
    override val onLastPage: Boolean get() = _onLastPage

    override suspend fun invoke(): List<PetWithProfilePic> {
        return currentSelection.value ?: listOf()
    }

    override fun resetCurrentPoint() {
    }
}