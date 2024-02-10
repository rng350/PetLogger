package com.hfad.petlogger

import android.content.Context
import java.io.File

object LocalStorageHandler {
    fun deleteFile(context: Context, filename: String) {
        val file = File(context.filesDir, filename)
        if (file.exists()) file.delete()
    }
}