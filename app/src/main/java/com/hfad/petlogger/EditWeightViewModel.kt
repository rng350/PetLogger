package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.fetchers.FetchWeightDetailsUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditWeightViewModel(val weightId: Long,
                          val weightDao: WeightDao): ViewModel() {
    val weight = MutableLiveData<Weight>()
    val pet = MutableLiveData<Pet>()
    val weightDateTime = SelectableDateTime()
    init {
        viewModelScope.launch {
            val fetchedWeightDetails = async {
                FetchWeightDetailsUseCase()(weightDao, weightId)
            }
            val weightDetails = fetchedWeightDetails.await()
            weight.value = weightDetails.weight
            pet.value = weightDetails.assocPet
            weight.value?.let {
                weightDateTime.set(it.weightDateTime)
            }
        }
    }

    fun submitChanges() {
        weight.value?.let {
            viewModelScope.launch {
                weightDao.update(Weight(it.id, pet.value?.petID ?: it.petId, it.weightGrams, weightDateTime.dateTime, it.weightNotes))
            }
        }
    }
}