package com.hfad.guineapiglog

data class CheckableItem<T>(val item: T, var isChecked: Boolean = false)