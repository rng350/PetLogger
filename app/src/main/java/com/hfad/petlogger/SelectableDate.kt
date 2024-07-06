package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.util.GetDateDisplayUseCase
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

// meant to be used as variable in viewmodels so we can get
// OffsetDateTimes from Instants given by DatePickers & TimePickers
// TODO: Maybe different display modes for dates & time
open class SelectableDate(initDateTime: OffsetDateTime = OffsetDateTime.now().withHour(0).withMinute(0)) {
    open var dateTime: OffsetDateTime = initDateTime
    open val dateDisplay: MutableLiveData<String> = MutableLiveData<String>(dateTime.toLocalDate().toString())
    val getDateDisplay = GetDateDisplayUseCase()

    open fun set(newDate: OffsetDateTime) {
        dateTime = newDate
        dateDisplay.value = getDateDisplay(dateTime)
    }

    open fun set(newDate: Instant) {
        // ZoneId set at UTC to prevent offset errors
        val pickedDate = OffsetDateTime
            .ofInstant(newDate, ZoneId.of("UTC"))

        set(dateTime
            .withYear(pickedDate.year)
            .withMonth(pickedDate.monthValue)
            .withDayOfMonth(pickedDate.dayOfMonth))
    }
}