package com.hfad.petlogger.common.search

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// expects YYYY-MM-DD input and validates it
class ValidateDateSearchUseCase {
    operator fun invoke(date: String): Boolean {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return try {
            LocalDate.parse(date, dateFormatter)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }
}