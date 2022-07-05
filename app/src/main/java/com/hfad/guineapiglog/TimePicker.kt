package com.hfad.guineapiglog

import android.content.Context
import android.text.format.DateFormat.is24HourFormat
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD

object TimePicker {
    fun generate(viewModel: WithDateTime, context: Context): MaterialTimePicker {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setTitleText("Select time")
            .setInputMode(INPUT_MODE_KEYBOARD)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            viewModel.dateTime.value?.let {
                var newOffsetDateTime = it.plusHours(timePicker.hour.toLong() - it.hour)
                newOffsetDateTime = newOffsetDateTime.plusMinutes(timePicker.minute.toLong() - it.minute)
                viewModel.dateTime.value = newOffsetDateTime
                viewModel.timeDisplay.value = newOffsetDateTime.toLocalTime().toString()
            }
        }

        return timePicker
    }
}