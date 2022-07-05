package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object Fetcher {
    fun fetchAllPets(viewModel: ViewModel, petsList: MutableLiveData<MutableList<Pet>>, petDao: PetDao) {
        viewModel.viewModelScope.launch {
            var fetchedPets = async {
                petDao.getAllAsync()
            }
            petsList.value = fetchedPets.await()
        }
    }

    fun fetchPet(viewModel: ViewModel, petDao: PetDao, petID: Long): Pet? {
        viewModel.viewModelScope.launch {

        }
        return null
    }

    fun fetchWeights(viewModel: ViewModel, weightsList: MutableLiveData<MutableList<Weight>>, weightDao: WeightDao, petID: Long? = null) {
        viewModel.viewModelScope.launch {
            val fetchedWeights = async {
                if (petID != null) weightDao.getPetWeights(petID) else weightDao.getAll()
            }
            weightsList.value = fetchedWeights.await()
        }
    }

    fun fetchWeight() {
    }
}