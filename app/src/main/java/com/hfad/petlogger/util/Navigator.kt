package com.hfad.petlogger.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class Navigator() {
    private val _navigateTo = MutableLiveData<Long?>()
    val navigateTo: LiveData<Long?>
        get() = _navigateTo

    fun navigateTo(id: Long) {
        _navigateTo.value = id
    }

    fun onNavigated() {
        _navigateTo.value = null
    }
}