package com.hfad.guineapiglog

import android.util.Log
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

object DatePicker {
    fun generate(viewModel: WithDateTime): MaterialDatePicker<Long> {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build()

        datePicker.addOnPositiveButtonClickListener {
            val oldOffsetDateTime = requireNotNull(viewModel.dateTime.value)
            // ZoneId set at UTC to prevent offset errors
            val pickedDate = OffsetDateTime
                .ofInstant(Instant.ofEpochMilli(it), ZoneId.of("UTC"))
                .toLocalDate()
            viewModel.dateTime.value = oldOffsetDateTime
                .withYear(pickedDate.year)
                .withMonth(pickedDate.monthValue)
                .withDayOfMonth(pickedDate.dayOfMonth)
            viewModel.dateDisplay.value = pickedDate.toString()
        }

        return datePicker
    }
}