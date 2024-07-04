package com.hfad.petlogger.util

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// Given two points in time of the day, what's the difference between them?
class GetTimeDifferenceUseCase(
    private val hourShortString: String = "h",
    private val minuteShortString: String = "m"
) {
    operator fun invoke(startDate: OffsetDateTime, endDate: OffsetDateTime = OffsetDateTime.now()): String {
        return invoke(startDate.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(), endDate.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
    }

    operator fun invoke(startDate: LocalDateTime, endDate: LocalDateTime): String {
        val startDateInMins = startDate.hour*60 + startDate.minute
        val endDateInMins = endDate.hour*60 + endDate.minute

        val higherVal = max(startDateInMins, endDateInMins)
        val lowerVal = min(startDateInMins, endDateInMins)

        val lowestDifference = min(higherVal-lowerVal, lowerVal+(24*60)-higherVal)

        val hoursDiff = lowestDifference / 60
        val minutesDiff = lowestDifference % 60
        return if (hoursDiff > 0) {
            if (minutesDiff > 0) "${hoursDiff}${hourShortString}${minutesDiff}${minuteShortString}"
            else "${hoursDiff}${hourShortString}"
        } else "${minutesDiff}${minuteShortString}"
    }
}