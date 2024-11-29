package com.hfad.petlogger.weights

import java.time.OffsetDateTime

interface FetchedWeight {
    val weightId: Long
    val weightGramsAmt: Int
    val weightDateTime: OffsetDateTime
}