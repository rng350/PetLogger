package com.hfad.petlogger.pets.domain.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.data.PetWithProfilePic

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