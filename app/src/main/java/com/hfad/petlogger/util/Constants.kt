package com.hfad.petlogger.util

import java.time.OffsetDateTime
import java.time.ZoneOffset

class Constants {
    companion object {
        val OFFSET_DATE_TIME_MAX_ALLOWED = OffsetDateTime.of(9999,12,31,1,1,1,1, ZoneOffset.UTC)
    }
}