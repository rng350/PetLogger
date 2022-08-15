package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import java.time.OffsetDateTime

class SelectableDateTime(initDateTime: OffsetDateTime = OffsetDateTime.now()): SelectableDate(initDateTime) {
    val timeDisplay: MutableLiveData<String> = MutableLiveData<String>(dateTime.toLocalTime().toString())

    override fun set(newDate: OffsetDateTime) {
        super.set(newDate)
        timeDisplay.value = dateTime.toLocalTime().toString()
    }
}