package com.hfad.petlogger.common.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

class GetDateDisplayUseCase {
    operator fun invoke(date: OffsetDateTime?): String {
        if (date == null) return "N/A"
        return invoke(date.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate())
    }
    operator fun invoke(date: LocalDateTime?): String {
        if (date == null) return "N/A"
        return invoke(date.toLocalDate())
    }
    operator fun invoke(date: LocalDate?): String {
        if (date == null) return "N/A"
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

        return "$month ${day.toString().padStart(2,'0')}, $year"
    }
}