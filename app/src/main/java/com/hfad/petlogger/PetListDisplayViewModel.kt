package com.hfad.petlogger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.fetchers.Fetcher
import kotlinx.coroutines.launch

class PetListDisplayViewModel(private val dao: PetDao): ViewModel() {
    private var _petsList = MutableLiveData<List<Pet>>()
    val petsList: LiveData<List<Pet>> = _petsList

    init {
        viewModelScope.launch {
            _petsList.value = Fetcher.fetchAllPets(dao)
        }
    }

    class Factory(private val dao: PetDao): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PetListDisplayViewModel::class.java)) {
                return PetListDisplayViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}