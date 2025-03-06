package com.hfad.petlogger.weights.data

import java.time.OffsetDateTime

// Mainly there for enforcing variable names to help prevent queries from crashing
interface FetchedWeight {
    val weightId: Long
    val weightGramsAmt: Int
    val weightDateTime: OffsetDateTime
}