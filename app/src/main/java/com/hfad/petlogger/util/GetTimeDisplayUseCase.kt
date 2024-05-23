package com.hfad.petlogger.util

import java.time.LocalDateTime
import java.time.OffsetDateTime

class GetTimeDisplayUseCase {
    operator fun invoke(date: OffsetDateTime): String {
        return invoke(date.toLocalDateTime())
    }
    operator fun invoke(date: LocalDateTime): String {
        return "${date.hour}:${date.minute}"
    }
}