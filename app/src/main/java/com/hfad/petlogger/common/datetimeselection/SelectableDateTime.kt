package com.hfad.petlogger.common.datetimeselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetTimeDisplayUseCase
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

// meant to be used as variable in viewmodels so we can get
// OffsetDateTimes from Instants given by DatePickers & TimePickers
class SelectableDateTime(private var _selectedDateTime: OffsetDateTime = OffsetDateTime.now(ZoneId.systemDefault())):
    SelectableDate {
    private val getDateDisplay = GetDateDisplayUseCase()
    private val _dateDisplay = MutableLiveData(getDateDisplay(_selectedDateTime))
    override val dateDisplay: LiveData<String> get() = _dateDisplay
    val selectedDateTime: OffsetDateTime get() = _selectedDateTime

    private val getTimeDisplay = GetTimeDisplayUseCase()
    private val _timeDisplay: MutableLiveData<String> = MutableLiveData<String>(getTimeDisplay(_selectedDateTime))
    val timeDisplay: LiveData<String> get() = _timeDisplay
    override fun set(newDate: Instant) {
        val localDate = newDate.atZone(ZoneId.of("UTC")).toLocalDate()
        val zoneId = ZoneId.systemDefault()
        val zonedDateTime = _selectedDateTime.atZoneSameInstant(zoneId)
        val currentSetTime = zonedDateTime.toLocalTime()
        val newZonedDateTime = ZonedDateTime.of(localDate, currentSetTime, zoneId)
        set(newZonedDateTime.toOffsetDateTime())
    }

    fun set(newDate: OffsetDateTime) {
        _selectedDateTime = newDate
        _dateDisplay.value = getDateDisplay(newDate)
        _timeDisplay.value = getTimeDisplay(newDate)
    }
}