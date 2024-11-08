package com.hfad.petlogger.common.util

class GetWeightGramsDisplayUseCase {
    private val GRAMS_THRESHOLD = 9999
    private val gramsUnit = "g"
    private val kilogramsUnit = "kg"
    private val measuringUnitConverter = MeasuringUnitConverter()

    operator fun invoke(weightGramsAmt: Int, autoConvertToKg: Boolean = true): String {
        return if (autoConvertToKg && weightGramsAmt>GRAMS_THRESHOLD) {
            "${measuringUnitConverter.gramsToKilograms(weightGramsAmt)}$kilogramsUnit"
        } else "$weightGramsAmt$gramsUnit"
    }
}