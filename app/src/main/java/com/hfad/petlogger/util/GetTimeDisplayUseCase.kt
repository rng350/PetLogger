package com.hfad.petlogger.util

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

class GetTimeDisplayUseCase {
    operator fun invoke(date: OffsetDateTime): String {
        return invoke(date.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
    }
    operator fun invoke(date: LocalDateTime): String {
        return "${date.hour.toString().padStart(2,'0')}:${date.minute.toString().padStart(2,'0')}"
    }
}