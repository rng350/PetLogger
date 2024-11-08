package com.hfad.petlogger.common

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// meant to be used as variable in viewmodels so we can get
// OffsetDateTimes from Instants given by DatePickers & TimePickers
open class SelectableDateOptional(private var _selectedDate: LocalDate? = null): SelectableDate {
    private val getDateDisplay = GetDateDisplayUseCase()
    private val _dateDisplay = MutableLiveData<String>(getDateDisplay(_selectedDate))
    override val dateDisplay: LiveData<String> get() = _dateDisplay
    val selectedDate: LocalDate?
        get() = _selectedDate

    override fun set(newDate: Instant) {
        val pickedDateLocal = newDate.atZone(ZoneId.of("UTC")).toLocalDate()
        Log.d("SelectableDate", "pickedDateLocal: ${pickedDateLocal.toString()}")
        _selectedDate = pickedDateLocal
        _dateDisplay.value = getDateDisplay(pickedDateLocal)
    }

    fun set(newDate: LocalDate) {
        _selectedDate = newDate
        _dateDisplay.value = getDateDisplay(newDate)
    }

    fun unSet() {
        _selectedDate = null
        _dateDisplay.value = "N/A"
    }
}