package com.hfad.petlogger.common.util

import com.hfad.petlogger.common.round
import kotlin.math.round

class MeasuringUnitConverter {
    fun kilogramsToGrams(weightInKg: Int): Int {
        return weightInKg * 1000
    }

    fun gramsToKilograms(weightInGrams: Int): Double {
        return (weightInGrams * 0.001).round(2)
    }

    fun poundsToGrams(weightInLbs: Int): Int {
        return round(weightInLbs * 453.59237).toInt()
    }

    fun gramsToPounds(weightInGrams: Int): Double {
        return (weightInGrams / 453.59237).round(2)
    }

    fun ouncesToGrams(weightInOz: Int): Int {
        return round(weightInOz * 28.349523125).toInt()
    }

    fun gramsToOunces(weightInGrams: Int): Double {
        return (weightInGrams / 28.34952312).round(2)
    }
}