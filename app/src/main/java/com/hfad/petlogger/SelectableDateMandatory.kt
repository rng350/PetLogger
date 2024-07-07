package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.util.GetDateDisplayUseCase
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

// meant to be used as variable in viewmodels so we can get
// OffsetDateTimes from Instants given by DatePickers & TimePickers
// TODO: Maybe different display modes for dates & time
open class SelectableDateMandatory(private var _selectedDateTime: OffsetDateTime = OffsetDateTime.now().withHour(0).withMinute(0)): SelectableDate {
    val selectedDateTime: OffsetDateTime get() = _selectedDateTime
    override val dateDisplay: MutableLiveData<String> = MutableLiveData<String>(_selectedDateTime.toLocalDate().toString())
    val getDateDisplay = GetDateDisplayUseCase()

    open fun set(newDate: OffsetDateTime) {
        _selectedDateTime = newDate
        dateDisplay.value = getDateDisplay(_selectedDateTime)
    }

    override fun set(newDate: Instant) {
        // ZoneId set at UTC to prevent offset errors
        val pickedDate = OffsetDateTime
            .ofInstant(newDate, ZoneId.of("UTC"))

        Log.d("SelectableDate", "pickedDateOffset: ${pickedDate.toString()}")

        set(selectedDateTime
            .withYear(pickedDate.year)
            .withMonth(pickedDate.monthValue)
            .withDayOfMonth(pickedDate.dayOfMonth))
    }
}