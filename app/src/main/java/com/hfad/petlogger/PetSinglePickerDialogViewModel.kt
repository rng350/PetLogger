package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.fetchers.FetchPetListForEditSelectionUseCase
import com.hfad.petlogger.fetchers.Fetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PetSinglePickerDialogViewModel(petDao: PetDao, initialPetSelection: Pet) : ViewModel() {
    var petListFetcher = FetchPetListForEditSelectionUseCase(petDao)
    var pets = MutableLiveData<List<CheckableItem<PetWithProfilePic>>>()
    val selectedPet = MutableLiveData<CheckableItem<PetWithProfilePic>>()

    init {
        viewModelScope.launch {
            pets.value = petListFetcher(initialPetSelection) ?: listOf<CheckableItem<PetWithProfilePic>>()
            pets.value?.first { it.isChecked.value == true }?.let {
                selectedPet.value = it
            }
            Log.d("PetPickerSingleDialog", "intiialized")
        }
    }

    companion object {
        fun provideFactory(petDao: PetDao, initialPetSelection: Pet): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetSinglePickerDialogViewModel::class.java)) {
                    return PetSinglePickerDialogViewModel(petDao, initialPetSelection) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}