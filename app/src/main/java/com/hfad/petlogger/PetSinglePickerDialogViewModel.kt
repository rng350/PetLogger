package com.hfad.petlogger

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
        }
    }
}