package com.hfad.petlogger.common

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ConfirmActionUseCase(
    private val context: Context,
    private val dialogTitle: String = "Confirmation",
    private val dialogMessage: String = "Are you sure you want to do this?",
    private val neutralButtonText: String = "Cancel",
    private val onNeutralButtonClick: ((dialog: DialogInterface, which: Int) -> Unit) = { dialog, which -> dialog.dismiss() },
    private val negativeButtonText: String = "No",
    private val onNegativeButtonClick: (dialog: DialogInterface, which: Int) -> Unit = { dialog, which -> dialog.cancel() },
    private val positiveButtonText: String = "Yes",
    private val onPositiveButtonClick: (dialog: DialogInterface, which: Int) -> Unit
) {
    operator fun invoke() {
        MaterialAlertDialogBuilder(context)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setNeutralButton(neutralButtonText, onNeutralButtonClick)
            .setNegativeButton(negativeButtonText, onNegativeButtonClick)
            .setPositiveButton(positiveButtonText, onPositiveButtonClick)
            .show()
    }
}