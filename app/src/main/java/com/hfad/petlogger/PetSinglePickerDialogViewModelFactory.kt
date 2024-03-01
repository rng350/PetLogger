package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.entities.Pet

class PetSinglePickerDialogViewModelFactory(private val petDao: PetDao, private val initialPetSelection: Pet): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetSinglePickerDialogViewModel::class.java)) {
            return PetSinglePickerDialogViewModel(petDao, initialPetSelection) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}