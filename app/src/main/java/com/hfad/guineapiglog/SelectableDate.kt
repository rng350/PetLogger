package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

open class SelectableDate(initDateTime: OffsetDateTime = OffsetDateTime.now()) {
    var dateTime: OffsetDateTime = initDateTime
    open val dateDisplay: MutableLiveData<String> = MutableLiveData<String>(dateTime.toLocalDate().toString())

    open fun set(newDate: OffsetDateTime) {
        dateTime = newDate
        dateDisplay.value = dateTime.toLocalDate().toString()
    }

    open fun set(newDate: Instant) {
        // ZoneId set at UTC to prevent offset errors
        val pickedDate = OffsetDateTime
            .ofInstant(newDate, ZoneId.of("UTC"))
            .toLocalDate()

        set(dateTime
            .withYear(pickedDate.year)
            .withMonth(pickedDate.monthValue)
            .withDayOfMonth(pickedDate.dayOfMonth))
    }
    // TODO: Maybe different display modes for dates & time
}