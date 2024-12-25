package com.hfad.petlogger.screens.weight.newweight

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.SelectableDateTime
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.Weight
import com.hfad.petlogger.weights.WeightRepository
import com.hfad.petlogger.common.util.MeasuringUnitConverter
import kotlinx.coroutines.launch

class NewWeightViewModel(
    private val weightRepository: WeightRepository
) : ViewModel() {
    val weightDateTime = SelectableDateTime()
    var weightAmt: MutableLiveData<Int> = MutableLiveData<Int>()
    var details: MutableLiveData<String> = MutableLiveData<String>()
    private val unitConverter = MeasuringUnitConverter()
    private var _unitType: String? = null
    val goToViewWeight = MutableLiveData<Long>()

    fun submitWeight(
        petId: Long,
        notes: List<Note> = listOf<Note>(),
        tags: List<Tag> = listOf<Tag>()
    ) {
        if (weightAmt.value != null && _unitType != null) {
            val weightDateTime = weightDateTime.selectedDateTime
            val convertedWeightAmt = when(_unitType) {
                "grams" -> { weightAmt.value!! }
                "kilograms" -> { unitConverter.kilogramsToGrams(weightAmt.value!!) }
                "pounds" -> { unitConverter.poundsToGrams(weightAmt.value!!) }
                "ounces" -> { unitConverter.ouncesToGrams(weightAmt.value!!)}
                else -> { weightAmt.value!! }
            }
            var weightNotes = ""
            details.value?.let {
                weightNotes = it
            }
            viewModelScope.launch {
                val weightAdded = weightRepository.addWeight(
                    weight = Weight(petId = petId, weightGrams = convertedWeightAmt, weightDateTime = weightDateTime, weightNotes = weightNotes),
                    notes = notes,
                    tags = tags
                )
                goToViewWeight.value = weightAdded.id
            }
        }
    }

    fun clearAll() {
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