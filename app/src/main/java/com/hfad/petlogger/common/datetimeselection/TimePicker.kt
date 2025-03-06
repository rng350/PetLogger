package com.hfad.petlogger.common.datetimeselection

import android.content.Context
import android.text.format.DateFormat.is24HourFormat
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object TimePicker {
    fun generate(oldDateTime: SelectableDateTime, context: Context): MaterialTimePicker {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setTitleText("Select time")
            .setInputMode(INPUT_MODE_KEYBOARD)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val localDate = oldDateTime.selectedDateTime.toLocalDate()
            val localTime = LocalTime.of(timePicker.hour, timePicker.minute)
            val zoneId = ZoneId.systemDefault()
            val zonedDateTime = ZonedDateTime.of(localDate, localTime, zoneId)
            val correctedDateTime = zonedDateTime.toOffsetDateTime()
            oldDateTime.set(correctedDateTime)
        }

        return timePicker
    }
}