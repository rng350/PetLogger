package com.hfad.petlogger.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

class GetDateDisplayUseCase {
    operator fun invoke(date: OffsetDateTime): String {
        return invoke(date.toLocalDate())
    }
    operator fun invoke(date: LocalDateTime): String {
        return invoke(date.toLocalDate())
    }
    operator fun invoke(date: LocalDate): String {
        val month =
            when(date.month) {
                java.time.Month.JANUARY -> "Jan"
                java.time.Month.FEBRUARY -> "Feb"
                java.time.Month.MARCH -> "Mar"
                java.time.Month.APRIL -> "Apr"
                java.time.Month.MAY -> "May"
                java.time.Month.JUNE -> "Jun"
                java.time.Month.JULY -> "Jul"
                java.time.Month.AUGUST -> "Aug"
                java.time.Month.SEPTEMBER -> "Sep"
                java.time.Month.OCTOBER -> "Oct"
                java.time.Month.NOVEMBER -> "Nov"
                java.time.Month.DECEMBER -> "Dec"
            }

        val day = date.dayOfMonth
        val year = date.year

        return "$month $day, $year"
    }
}