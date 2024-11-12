package com.hfad.petlogger.common.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NewEntityNavigator {
    private val _makeNewEntity = MutableLiveData(false)
    val makeNewEntity: LiveData<Boolean> get() = _makeNewEntity

    fun navigateToNewEntityScreen() {
        _makeNewEntity.value = true
    }

    fun onNavigatedToNewEntityScreen() {
        _makeNewEntity.value = false
    }
}