package com.hfad.petlogger.util

import java.time.LocalDateTime
import java.time.OffsetDateTime

class GetDateTimeDisplayUseCase {
    private val dateDisplayUseCase = GetDateDisplayUseCase()
    private val timeDisplayUseCase = GetTimeDisplayUseCase()

    operator fun invoke(dateTime: OffsetDateTime): String {
        return invoke(dateTime.toLocalDateTime())
    }

    operator fun invoke(dateTime: LocalDateTime): String {
        return "${dateDisplayUseCase(dateTime)} at ${timeDisplayUseCase.invoke(dateTime)}"
    }
}