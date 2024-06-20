package com.hfad.petlogger.util

import android.util.Log
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Period

class GetPeriodDisplayUseCase(
    private val yearSingularString: String = "year",
    private val yearPluralString: String = "years",
    private val monthSingularString: String = "month",
    private val monthPluralString: String = "months",
    private val daySingularString: String = "day",
    private val dayPluralString: String = "days"
) {
    operator fun invoke(startDate: OffsetDateTime, endDate: OffsetDateTime? = null): String {
        return if (endDate != null) {
            getPeriodDisplay(startDate, endDate)
        } else {
            getPeriodDisplay(startDate)
        }
    }

    operator fun invoke(startDate: LocalDate, endDate: LocalDate? = null): String {
        return if (endDate != null) {
            getPeriodDisplay(startDate, endDate)
        } else {
            getPeriodDisplay(startDate)
        }
    }
    private fun getPeriodDisplay(startDate: OffsetDateTime, endDate: OffsetDateTime = OffsetDateTime.now()): String {
        return getPeriodDisplay(startDate.toLocalDate(), endDate.toLocalDate())
    }
    private fun getPeriodDisplay(startDate: LocalDate, endDate: LocalDate = LocalDate.now()): String {
        val period = Period.between(startDate, endDate)
        return if (period.years > 0)
            "${period.years} ${getYearsLabel(period.years)}, " +
                    "${period.months} ${getMonthsLabel(period.months)}, " +
                    "${period.days} ${getDaysLabel(period.days)}"
        else if (period.months > 0)
            "${period.months} ${getMonthsLabel(period.months)}, " +
                    "${period.days} ${getDaysLabel(period.days)}"
        else "${period.days} ${getDaysLabel(period.days)}"
    }

    private fun getYearsLabel(years: Int): String {
        return if (years == 1) yearSingularString else yearPluralString
    }
    private fun getMonthsLabel(months: Int): String {
        return if (months == 1) monthSingularString else monthPluralString
    }
    private fun getDaysLabel(days: Int): String {
        return if (days == 1) daySingularString else dayPluralString
    }
}