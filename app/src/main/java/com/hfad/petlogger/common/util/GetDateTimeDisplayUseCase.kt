package com.hfad.petlogger.common.util

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

class GetDateTimeDisplayUseCase {
    private val dateDisplayUseCase = GetDateDisplayUseCase()
    private val timeDisplayUseCase = GetTimeDisplayUseCase()

    operator fun invoke(dateTime: OffsetDateTime?): String {
        if (dateTime==null) return "N/A"
        return invoke(dateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
    }

    operator fun invoke(dateTime: LocalDateTime?): String {
        if (dateTime==null) return "N/A"
        return "${dateDisplayUseCase(dateTime)} at ${timeDisplayUseCase.invoke(dateTime)}"
    }
}