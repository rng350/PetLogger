package com.hfad.petlogger.common.util

import android.util.Log
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
    operator fun invoke(date: LocalDate?, monthInFull: Boolean = false): String {
        if (date == null) return "N/A"
        val month = if (monthInFull) {
            when(date.month) {
                java.time.Month.JANUARY -> "January"
                java.time.Month.FEBRUARY -> "February"
                java.time.Month.MARCH -> "March"
                java.time.Month.APRIL -> "April"
                java.time.Month.MAY -> "May"
                java.time.Month.JUNE -> "June"
                java.time.Month.JULY -> "July"
                java.time.Month.AUGUST -> "August"
                java.time.Month.SEPTEMBER -> "September"
                java.time.Month.OCTOBER -> "October"
                java.time.Month.NOVEMBER -> "November"
                java.time.Month.DECEMBER -> "December"
            }
        } else {
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
        }

        val day = date.dayOfMonth
        val year = date.year

        return "$month ${day.toString().padStart(2,'0')}, $year"
    }
}