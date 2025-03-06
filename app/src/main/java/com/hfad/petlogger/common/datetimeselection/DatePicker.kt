package com.hfad.petlogger.common.datetimeselection

import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant

object DatePicker {
    fun generate(oldDateTime: SelectableDate): MaterialDatePicker<Long> {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build()

        datePicker.addOnPositiveButtonClickListener {
            oldDateTime.set(Instant.ofEpochMilli(it))
        }

        return datePicker
    }
}