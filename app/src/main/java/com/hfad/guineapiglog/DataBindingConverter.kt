package com.hfad.guineapiglog

import android.widget.EditText
import androidx.databinding.InverseMethod

object DataBindingConverter {
    @InverseMethod("stringToInt")
    @JvmStatic
    fun intToString(view: EditText, value: Int): String {
        return value.toString()
    }

    @JvmStatic
    fun stringToInt(view: EditText, value: String): Int {
        if (value == "") return 0
        return value.toInt()
    }
}