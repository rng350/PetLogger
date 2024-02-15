package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PetListViewModel(val petDao: PetDao) : ViewModel() {
    val pets = MutableLiveData<List<PetWithProfilePic>>()
    val petNavigator = Navigator()
    init {
        viewModelScope.launch(Dispatchers.IO) {
            pets.postValue(Fetcher.fetchPetsWithProfilePhotos(petDao))
        }
    }
}