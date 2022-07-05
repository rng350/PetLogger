package com.hfad.guineapiglog

import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

object DatePicker {
    fun generate(viewModel: WithDateTime): MaterialDatePicker<Long> {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build()

        datePicker.addOnPositiveButtonClickListener {
            val oldOffsetDateTime = requireNotNull(viewModel.dateTime.value)
            var newOffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            newOffsetDateTime = newOffsetDateTime.plusHours(oldOffsetDateTime.hour - newOffsetDateTime.hour.toLong())
            newOffsetDateTime = newOffsetDateTime.plusMinutes(oldOffsetDateTime.minute - newOffsetDateTime.minute.toLong())
            viewModel.dateTime.value = newOffsetDateTime

            viewModel.dateDisplay.value = viewModel.dateTime.value?.toString()
            // Log.i("TIME", "added date ${viewModel.eventDateDisplay}")
        }

        return datePicker
    }
}