package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.fetchers.FetchWeightDetailsUseCase
import com.hfad.petlogger.repositories.WeightRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditWeightViewModel(private val weightRepository: WeightRepository, private val weightId: Long): ViewModel() {
    val weight = MutableLiveData<Weight>()
    val weightDateTime = SelectableDateTime()
    init {
        viewModelScope.launch {
            val fetchedWeightDetails = async {
                weightRepository.getWeightDetails(weightId)
            }
            val weightDetails = fetchedWeightDetails.await()
            weight.value = weightDetails.weight
            weight.value?.let {
                weightDateTime.set(it.weightDateTime)
            }
        }
    }

    fun submitChanges(pet: Pet) {
        weight.value?.let {
            viewModelScope.launch {
                weightRepository.update(Weight(it.id, pet.petID, it.weightGrams, weightDateTime.dateTime, it.weightNotes))
            }
        }
    }

    companion object {
        fun provideFactory(weightRepository: WeightRepository, weightId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EditWeightViewModel::class.java)) {
                    return EditWeightViewModel(weightRepository, weightId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}