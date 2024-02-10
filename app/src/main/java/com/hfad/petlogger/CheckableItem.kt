package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData

data class CheckableItem<T>(val item: T, var isChecked: MutableLiveData<Boolean> = MutableLiveData(false))