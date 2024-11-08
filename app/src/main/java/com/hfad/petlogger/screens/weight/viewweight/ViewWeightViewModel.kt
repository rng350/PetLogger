package com.hfad.petlogger.screens.weight.viewweight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.weights.WeightFullDetailsState
import com.hfad.petlogger.weights.WeightRepository
import com.hfad.petlogger.common.util.MeasuringUnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewWeightViewModel(weightRepository: WeightRepository, weightId: Long): ViewModel() {
    private val _fullWeightDetails: MutableStateFlow<WeightFullDetailsState?> = MutableStateFlow<WeightFullDetailsState?>(null)
    val fullWeightDetails: StateFlow<WeightFullDetailsState?> = _fullWeightDetails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    private val unitType = MutableStateFlow<WeightUnitType>(WeightUnitType.GRAMS)
    val curWeightAmtDisplay: StateFlow<String> = fullWeightDetails.combine(unitType) { weightDetails, unit ->
        var display = ""
        weightDetails?.let {
            display = getWeightDisplay(weightGramsAmt=weightDetails.curWeight.weightGrams, weightUnitType = unit)
        }
        display
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )
    val prevWeightAmtDisplay: StateFlow<String> = fullWeightDetails.combine(unitType) { weightDetails, unit ->
        var display = ""
        weightDetails?.prevWeight?.let {
            display = getWeightDisplay(weightGramsAmt=it.weightGrams, weightUnitType = unit)
        }
        display
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )
    val weightDifferenceDisplay: StateFlow<String> = fullWeightDetails.combine(unitType) { weightDetails, unit ->
        var display = ""
        weightDetails?.prevWeight?.let {
            val weightDifferenceQtyDisplay = getWeightDisplay(it.weightDifferenceGrams, unit)
            display = "${if (it.weightDifferenceGrams>=0)"+" else ""}$weightDifferenceQtyDisplay"
        }
        display
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    private val measuringUnitConverter = MeasuringUnitConverter()

    init {
        viewModelScope.launch {
            _fullWeightDetails.value = weightRepository.getWeightFullDetails(weightId)
        }
    }

    fun setWeightUnit(weightUnit: String) {
        unitType.update {
            when (weightUnit) {
                "g" -> WeightUnitType.GRAMS
                "kg" -> WeightUnitType.KILOGRAMS
                "oz" -> WeightUnitType.OUNCES
                else -> WeightUnitType.POUNDS
            }
        }
    }

    private fun getWeightDisplay(weightGramsAmt: Int, weightUnitType: WeightUnitType): String {
        val weightQty: String
        val unit: String
        when (weightUnitType) {
            WeightUnitType.GRAMS -> {
                weightQty = "$weightGramsAmt"
                unit = "g"
            }
            WeightUnitType.KILOGRAMS ->  {
                weightQty = "${measuringUnitConverter.gramsToKilograms(weightGramsAmt)}"
                unit = "kg"
            }
            WeightUnitType.OUNCES -> {
                weightQty = "${measuringUnitConverter.gramsToOunces(weightGramsAmt)}"
                unit = "oz"
            }
            WeightUnitType.POUNDS -> {
                weightQty = "${measuringUnitConverter.gramsToPounds(weightGramsAmt)}"
                unit = "lb"
            }
        }
        return "$weightQty $unit"
    }

    enum class WeightUnitType {
        GRAMS, KILOGRAMS, OUNCES, POUNDS
    }

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