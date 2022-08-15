package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import java.time.Instant
import java.time.OffsetDateTime

open class SelectableDateOptional(initDateTime: OffsetDateTime = OffsetDateTime.MIN): SelectableDate(initDateTime) {
    override val dateDisplay: MutableLiveData<String> = MutableLiveData<String>("N/A")
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