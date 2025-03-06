package com.hfad.petlogger.common.util

import android.widget.EditText
import androidx.databinding.InverseMethod

object DataBindingConverter {
    @InverseMethod("stringToInt")
    @JvmStatic
    fun intToString(view: EditText, value: Int?): String {
        if (value == null) return ""
        return value.toString()
    }

    @JvmStatic
    fun stringToInt(view: EditText, value: String): Int? {
        if (value == "") return null
        return value.toInt()
    }

    //TextInputEditText
    @InverseMethod("stringToIntMaterial")
    @JvmStatic
    fun intToStringMaterial(value: Int?): String {
        if (value == null) return ""
        return value.toString()
    }

    @JvmStatic
    fun stringToIntMaterial(value: String): Int? {
        if (value == "") return Int.MIN_VALUE
        return value.toInt()
    }

    @InverseMethod("stringToDouble")
    @JvmStatic
    fun doubleToString(value: Double?): String {
        value?.let {
            return it.toString()
        }
        return ""
    }

    @JvmStatic
    fun stringToDouble(value: String): Double? {
        if (value == "") return Double.MIN_VALUE
        return value.toDouble()
    }
}