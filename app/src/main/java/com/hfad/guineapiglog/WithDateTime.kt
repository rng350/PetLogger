package com.hfad.guineapiglog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.OffsetDateTime

interface WithDateTime {
    var dateTime: MutableLiveData<OffsetDateTime>
    var dateDisplay: MutableLiveData<String>
    var timeDisplay: MutableLiveData<String>
}