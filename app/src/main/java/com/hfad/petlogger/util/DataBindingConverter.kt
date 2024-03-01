package com.hfad.petlogger.util

import android.widget.EditText
import androidx.databinding.InverseMethod
import com.google.android.material.textfield.TextInputEditText

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
        if (value == "") return 0
        return value.toInt()
    }
}