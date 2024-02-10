package com.hfad.petlogger.util

import android.net.Uri
import androidx.room.TypeConverter

object URIConverter {
    @TypeConverter
    @JvmStatic
    fun toString(value: Uri?): String? {
        value?.let {
            return value.toString()
        }
        return null
    }

    @TypeConverter
    @JvmStatic
    fun toURI(value: String?): Uri? {
        value?.let {
            return Uri.parse(value)
        }
        return null
    }
}