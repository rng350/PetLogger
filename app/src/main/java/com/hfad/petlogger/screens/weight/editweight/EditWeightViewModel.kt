package com.hfad.petlogger.screens.weight.editweight

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.datetimeselection.SelectableDateTime
import com.hfad.petlogger.common.util.GetDateTimeDisplayUseCase
import com.hfad.petlogger.common.util.MeasuringUnitConverter
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.tags.data.Tag
import com.hfad.petlogger.weights.data.Weight
import com.hfad.petlogger.weights.domain.WeightRepository
import com.hfad.petlogger.weights.domain.usecases.GetSingleWeightUseCase
import kotlinx.coroutines.launch

class EditWeightViewModel(
    private val weightRepository: WeightRepository,
    private val getWeight: GetSingleWeightUseCase
): ViewModel() {
    private val _weightPetName = MutableLiveData<String>("")
    val weightPetName: LiveData<String> get() = _weightPetName
    private val _initWeightDateTimeDisplay = MutableLiveData<String>("")
    val initWeightDateTimeDisplay: LiveData<String> get() = _initWeightDateTimeDisplay
    val weight = MutableLiveData<Weight>()
    val weightGrams = MutableLiveData<Int>()
    val weightDateTime = SelectableDateTime()
    private var _unitType: String = "grams"
    val unitType: String get() = _unitType
    private val unitConverter = MeasuringUnitConverter()
    private val _goToWeightsList = MutableLiveData(false)
    val goToWeightsList: LiveData<Boolean> get() = _goToWeightsList
    private val _goToViewWeight = MutableLiveData(false)
    val goToViewWeight: LiveData<Boolean> get() = _goToViewWeight
    init {
        viewModelScope.launch {
            val weightDetails = getWeight()
            _weightPetName.value = weightDetails.petName
            val getWeightDateTimeDisplay = GetDateTimeDisplayUseCase()
            _initWeightDateTimeDisplay.value = getWeightDateTimeDisplay(weightDetails.weight.weightDateTime)
            weight.value = weightDetails.weight
            weight.value?.let {
                weightDateTime.set(it.weightDateTime)
            }
        }
    }

    fun submitChanges(
        petId: Long,
        notesToAdd: List<Note> = listOf<Note>(),
        notesToRemove: List<Note> = listOf<Note>(),
        notesToUpdate: List<Note> = listOf<Note>(),
        tagsToAdd: List<Tag> = listOf<Tag>(),
        tagsToRemove: List<Tag> = listOf<Tag>()
    ) {
        weight.value?.let { weightVal ->
            val convertedWeightAmt = when(_unitType) {
                "grams" -> { weightVal.weightGrams }
                "kilograms" -> { unitConverter.kilogramsToGrams(weightVal.weightGrams) }
                "pounds" -> { unitConverter.poundsToGrams(weightVal.weightGrams) }
                "ounces" -> { unitConverter.ouncesToGrams(weightVal.weightGrams)}
                else -> { weightVal.weightGrams }
            }

            viewModelScope.launch {
                weightRepository.update(
                    weight = Weight(weightVal.id, petId, convertedWeightAmt, weightDateTime.selectedDateTime, weightVal.weightNotes),
                    notesToAdd = notesToAdd,
                    notesToRemove = notesToRemove,
                    notesToUpdate = notesToUpdate,
                    tagsToAdd = tagsToAdd,
                    tagsToRemove = tagsToRemove
                )
                _goToViewWeight.value = true
            }
        }
    }

    fun deleteWeight() {
        weight.value?.let {
            viewModelScope.launch {
                weightRepository.delete(it)
                _goToWeightsList.value = true
            }
        }
    }

    fun onNavigateToWeightsList() {
        _goToWeightsList.value = false
    }

    fun onNavigateToViewWeight() {
        _goToViewWeight.value = false
    }

    fun setWeightUnitType(unitType: String) {
        _unitType = unitType
    }

    companion object {
        fun provideFactory(weightRepository: WeightRepository, getWeight: GetSingleWeightUseCase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EditWeightViewModel::class.java)) {
                    return EditWeightViewModel(weightRepository, getWeight) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}