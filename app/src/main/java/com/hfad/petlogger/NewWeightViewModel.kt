package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.WeightRepository
import com.hfad.petlogger.util.MeasuringUnitConverter
import kotlinx.coroutines.launch

class NewWeightViewModel(
    private val weightRepository: WeightRepository
) : ViewModel() {
    val weightDateTime = SelectableDateTime()
    var petNameDisplay: MutableLiveData<String> = MutableLiveData<String>()
    var weightAmt: MutableLiveData<Int> = MutableLiveData<Int>()
    var details: MutableLiveData<String> = MutableLiveData<String>()
    private val unitConverter = MeasuringUnitConverter()
    private var _unitType: String? = null

    fun submitWeight(pet: Pet) {
        if (weightAmt.value != null && _unitType != null) {
            val weight = Weight()
            weight.weightDateTime = weightDateTime.dateTime
            weight.petId = pet.petID
            val convertedWeight = when(_unitType) {
                "grams" -> { weightAmt.value!! }
                "kilograms" -> { unitConverter.kilogramsToGrams(weightAmt.value!!) }
                "pounds" -> { unitConverter.poundsToGrams(weightAmt.value!!) }
                "ounces" -> { unitConverter.ouncesToGrams(weightAmt.value!!)}
                else -> { weightAmt.value!! }
            }
            weight.weightGrams = convertedWeight
            details.value?.let {
                weight.weightNotes = it
            }
            viewModelScope.launch {
                weightRepository.insert(weight)
            }
        }
    }

    fun setWeightUnitType(unitType: String) {
        _unitType = unitType
    }

    companion object {
        fun provideFactory(weightRepository: WeightRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NewWeightViewModel::class.java)) {
                    return NewWeightViewModel(weightRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}