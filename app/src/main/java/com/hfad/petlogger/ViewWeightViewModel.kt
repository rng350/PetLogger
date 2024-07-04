package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.repositories.WeightRepository
import com.hfad.petlogger.util.GetDateTimeDisplayUseCase
import com.hfad.petlogger.util.GetPeriodDisplayUseCase
import com.hfad.petlogger.util.GetTimeDifferenceUseCase
import com.hfad.petlogger.util.MeasuringUnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ViewWeightViewModel(weightRepository: WeightRepository, weightId: Long): ViewModel() {
    val weight: StateFlow<Weight?> = weightRepository.getWeight(weightId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val prevWeight: StateFlow<Weight?> = weightRepository.getPreviousWeight(weightId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val assocPet: StateFlow<PetWithProfilePic?> = weightRepository.getPetOfWeight(weightId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    private val unitType = MutableStateFlow<WeightUnitType>(WeightUnitType.GRAMS)

    val getDateTimeDisplayUseCase = GetDateTimeDisplayUseCase()
    val weightAmtDisplay: StateFlow<WeightDisplay> = asWeightDisplay(weight)
    val prevWeightAmtDisplay: StateFlow<WeightDisplay> = asWeightDisplay(prevWeight)

    val weightDifferenceDisplay = weight.combine(prevWeight) { viewedWeight, prevWeight ->
        if (viewedWeight!=null && prevWeight!=null) {
            viewedWeight.weightGrams-prevWeight.weightGrams
        } else null
    }.combine(unitType) { difference, unit ->
        if (difference != null) {
            "${convertWeight(difference)}${getWeightUnitTypeDisplay()}"
        } else ""
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val getDateDifferenceUseCase = GetPeriodDisplayUseCase()
    val dateDifferenceDisplay = weight.combine(prevWeight) { viewedWeight, prevWeight ->
        if (viewedWeight!=null && prevWeight!=null) {
            "${getDateDifferenceUseCase.getPeriodDisplayShort(viewedWeight.weightDateTime, prevWeight.weightDateTime)} ago"
        } else ""
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val getTimeDifferenceUseCase = GetTimeDifferenceUseCase()
    val timeDifferenceDisplay = weight.combine(prevWeight) { viewedWeight, prevWeight ->
        if (viewedWeight!=null && prevWeight!=null) {
            "${getTimeDifferenceUseCase(viewedWeight.weightDateTime, prevWeight.weightDateTime)} diff"
        } else ""
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    private val _measuringUnitConverter = MeasuringUnitConverter()

    private fun convertWeight(weightGrams: Int): Double {
        return when (unitType.value) {
            WeightUnitType.GRAMS -> weightGrams.toDouble()
            WeightUnitType.KILOGRAMS -> _measuringUnitConverter.gramsToKilograms(weightGrams)
            WeightUnitType.OUNCES -> _measuringUnitConverter.gramsToOunces(weightGrams)
            else -> _measuringUnitConverter.gramsToPounds(weightGrams)
        }
    }

    fun setWeightUnit(weightUnit: String) {
        unitType.value = when (weightUnit) {
            "g" -> WeightUnitType.GRAMS
            "kg" -> WeightUnitType.KILOGRAMS
            "oz" -> WeightUnitType.OUNCES
            else -> WeightUnitType.POUNDS
        }
    }

    private fun asWeightDisplay(flow: StateFlow<Weight?>): StateFlow<WeightDisplay> {
        return flow.combine(unitType) { f1, f2 ->
            WeightDisplay(
                weightAmt = if (f1 != null) convertWeight(f1.weightGrams).toString() else "",
                weightUnit = getWeightUnitTypeDisplay(),
                weightDateTime = if (f1 != null) getDateTimeDisplayUseCase(f1.weightDateTime) else ""
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeightDisplay("","", "")
        )
    }

    private fun getWeightUnitTypeDisplay(): String {
        return when(unitType.value) {
            WeightUnitType.GRAMS -> "g"
            WeightUnitType.KILOGRAMS -> "kg"
            WeightUnitType.OUNCES -> "oz"
            else -> "lb"
        }
    }

    enum class WeightUnitType {
        GRAMS, KILOGRAMS, OUNCES, POUNDS
    }

    data class WeightDisplay(val weightAmt: String, val weightUnit: String, val weightDateTime: String)

    companion object {
        fun provideFactory(weightRepository: WeightRepository, weightId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ViewWeightViewModel::class.java)) {
                    return ViewWeightViewModel(weightRepository, weightId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}