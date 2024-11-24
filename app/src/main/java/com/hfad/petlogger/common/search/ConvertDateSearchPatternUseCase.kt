package com.hfad.petlogger.common.search

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// expects YYYY-MM-DD input and validates it
class ConvertDateSearchPatternUseCase {
    operator fun invoke(date: String): LocalDate? {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return try {
            LocalDate.parse(date, dateFormatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }
}