package com.hfad.petlogger.common.util

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class DisplayTimeAgoUseCase(
    private val daySingularString: String = "day",
    private val dayPluralString: String = "days",
    private val hourSingularString: String = "hour",
    private val hourPluralString: String = "hours",
    private val minuteSingularString: String = "minute",
    private val minutePluralString: String = "minutes",
    private val justNowString: String = "Just now",
    private val neverString: String = "Never"
) {
    operator fun invoke(date: OffsetDateTime?): String {
        date?.let {
            val daysBetween = ChronoUnit.DAYS.between(date, OffsetDateTime.now())
            if (daysBetween > 1L) {
                return "$daysBetween $dayPluralString ago"
            }
            else if (daysBetween == 1L) {
                return "$daysBetween $daySingularString ago"
            }
            val hoursBetween = ChronoUnit.HOURS.between(date, OffsetDateTime.now()) % 24
            if (hoursBetween > 1L) {
                return "$hoursBetween $hourPluralString ago"
            }
            else if (hoursBetween == 1L) {
                return "$hoursBetween $hourSingularString ago"
            }
            val minuteBetween = ChronoUnit.MINUTES.between(date, OffsetDateTime.now()) % 60
            if (minuteBetween > 1L) {
                return "$minuteBetween $minutePluralString ago"
            }
            else if (minuteBetween == 1L) {
                return "$minuteBetween $minuteSingularString ago"
            } else {
                return justNowString
            }
        }
        return neverString
    }
}