package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import java.time.Instant
import java.time.OffsetDateTime

// meant to be used as variable in viewmodels so we can get
// OffsetDateTimes from Instants given by DatePickers & TimePickers
open class SelectableDateOptional(initDateTime: OffsetDateTime = OffsetDateTime.now().withHour(0).withMinute(0)): SelectableDate(initDateTime) {
    override val dateDisplay: MutableLiveData<String> = MutableLiveData<String>("N/A")
    val selectedDate: OffsetDateTime?
        get() = if (hasBeenSet) super.dateTime else null
    var hasBeenSet: Boolean = false

    override fun set(newDate: OffsetDateTime) {
        super.set(newDate)
        hasBeenSet = true
    }

    override fun set(newDate: Instant) {
        super.set(newDate)
        hasBeenSet = true
    }

    fun unSet() {
        hasBeenSet = false
    }
}