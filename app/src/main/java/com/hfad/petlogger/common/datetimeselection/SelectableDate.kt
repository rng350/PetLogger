package com.hfad.petlogger.common.datetimeselection

import androidx.lifecycle.LiveData
import java.time.Instant

// for
interface SelectableDate {
    val dateDisplay: LiveData<String>
    fun set(newDate: Instant)
}