package com.hfad.guineapiglog

import android.util.Log
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

object DatePicker {
    fun generate(oldDateTime: SelectableDate): MaterialDatePicker<Long> {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build()

        datePicker.addOnPositiveButtonClickListener {
            // ZoneId set at UTC to prevent offset errors
            /*val pickedDate = OffsetDateTime
                .ofInstant(Instant.ofEpochMilli(it), ZoneId.of("UTC"))
                .toLocalDate()
            oldDateTime.set(oldDateTime.dateTime
                .withYear(pickedDate.year)
                .withMonth(pickedDate.monthValue)
                .withDayOfMonth(pickedDate.dayOfMonth))*/
            oldDateTime.set(Instant.ofEpochMilli(it))
        }

        return datePicker
    }
}