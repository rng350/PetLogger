package com.hfad.petlogger.common.photoselection

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Implementation of [ActivityResultContracts.GetMultipleContents] that allows the caller to change maxItems at runtime
 */
class AdjustablePickMultipleVisualMedia(private var maxItems:Int): ActivityResultContracts.PickMultipleVisualMedia(maxItems){
    fun updateMaxItems(newMaxItems:Int){
        maxItems = newMaxItems
        Log.d("ADJ", "MaxItems changed")
    }
    override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {
        return super.createIntent(context, input).apply {
            if (isSystemPickerAvailable()) {
                putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxItems)
            }
            else if (isSystemFallbackPickerAvailable(context)) {
                putExtra("androidx.activity.result.contract.extra.PICK_IMAGES_MAX", maxItems)
            }
            else if (isGmsPickerAvailable(context)) {
                putExtra("com.google.android.gms.provider.extra.PICK_IMAGES_MAX", maxItems)
            }
        }
    }

    companion object {
        fun isSystemPickerAvailable(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                true
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // getExtension is seen as part of Android Tiramisu only while the SdkExtensions
                // have been added on Android R
                SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2
            } else {
                false
            }
        }
        internal fun isSystemFallbackPickerAvailable(context: Context): Boolean {
            return getSystemFallbackPicker(context) != null
        }

        @Suppress("DEPRECATION")
        @JvmStatic
        internal fun getSystemFallbackPicker(context: Context): ResolveInfo? {
            return context.packageManager.resolveActivity(
                Intent("androidx.activity.result.contract.action.PICK_IMAGES"),
                PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_SYSTEM_ONLY
            )
        }

        @JvmStatic
        internal fun isGmsPickerAvailable(context: Context): Boolean {
            return getGmsPicker(context) != null
        }

        @Suppress("DEPRECATION")
        @JvmStatic
        internal fun getGmsPicker(context: Context): ResolveInfo? {
            return context.packageManager.resolveActivity(
                Intent("com.google.android.gms.provider.action.PICK_IMAGES"),
                PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_SYSTEM_ONLY
            )
        }
    }
}