package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import java.time.OffsetDateTime

// meant to be used as variable in viewmodels so we can get
// OffsetDateTimes from Instants given by DatePickers & TimePickers
class SelectableDateTime(initDateTime: OffsetDateTime = OffsetDateTime.now()): SelectableDate(initDateTime) {
    val timeDisplay: MutableLiveData<String> = MutableLiveData<String>(dateTime.toLocalTime().toString())

    override fun set(newDate: OffsetDateTime) {
        super.set(newDate)
        timeDisplay.value = dateTime.toLocalTime().toString()
    }
}