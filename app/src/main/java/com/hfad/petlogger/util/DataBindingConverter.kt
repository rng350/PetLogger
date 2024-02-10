package com.hfad.petlogger.util

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
}