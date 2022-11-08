package com.hfad.guineapiglog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import androidx.fragment.app.DialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD

object TimePicker {
    fun generate(oldDateTime: SelectableDateTime, context: Context): MaterialTimePicker {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setTitleText("Select time")
            .setInputMode(INPUT_MODE_KEYBOARD)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            oldDateTime.set(oldDateTime.dateTime
                .withHour(timePicker.hour)
                .withMinute(timePicker.minute)
                .withSecond(0))
        }

        return timePicker
    }
}