package com.hfad.petlogger

import androidx.lifecycle.LiveData
import java.time.Instant

// for
interface SelectableDate {
    val dateDisplay: LiveData<String>
    fun set(newDate: Instant)
}