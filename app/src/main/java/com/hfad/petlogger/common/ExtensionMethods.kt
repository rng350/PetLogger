package com.hfad.petlogger.common

import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import java.io.File

val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024
val File.sizeInGb get() = sizeInMb / 1024
val File.sizeInTb get() = sizeInGb / 1024

fun Uri.asFile(): File = File(toString())

fun String?.asUri(): Uri? {
    try {
        return Uri.parse(this)
    } catch (e: Exception) {
    }
    return null
}

fun File.sizeStr(): String = size.toString()
fun File.sizeStrInKb(decimals: Int = 0): String = "%.${decimals}f".format(sizeInKb)
fun File.sizeStrInMb(decimals: Int = 0): String = "%.${decimals}f".format(sizeInMb)
fun File.sizeStrInGb(decimals: Int = 0): String = "%.${decimals}f".format(sizeInGb)

fun File.sizeStrWithBytes(): String = sizeStr() + "b"
fun File.sizeStrWithKb(decimals: Int = 0): String = sizeStrInKb(decimals) + "Kb"
fun File.sizeStrWithMb(decimals: Int = 0): String = sizeStrInMb(decimals) + "Mb"
fun File.sizeStrWithGb(decimals: Int = 0): String = sizeStrInGb(decimals) + "Gb"

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}

fun <T> List<T>.copyOf(): List<T> {
    return mutableListOf<T>().also { it.addAll(this) }
}

fun <T> List<T>.mutableCopyOf(): MutableList<T> {
    return mutableListOf<T>().also { it.addAll(this) }
}

// prevent a crash that occurs when quick successive calls to Navigation functions are made
fun NavController.navigateSafe(directions: NavDirections, navOptions: NavOptions? = null) {
    currentDestination?.getAction(directions.actionId)?.let {
        navigate(directions, navOptions)
    }
}

fun Double.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()