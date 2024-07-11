package com.hfad.petlogger.entities

import android.net.Uri
import java.time.OffsetDateTime

data class WeightForListFetched(
    val weightId: Long,
    val weightGramsAmt: Int,
    val weightDateTime: OffsetDateTime,
    val weightPetName: String,
    val weightPetPhotoUri: Uri?
)
