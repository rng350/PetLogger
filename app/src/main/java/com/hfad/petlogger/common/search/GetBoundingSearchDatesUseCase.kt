package com.hfad.petlogger.common.search

import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

// Even if there aren't any
// If it returns null, assume
class GetBoundingSearchDatesUseCase {
    operator fun invoke(beforeDates: List<String>?, afterDates: List<String>?): Result {
        // validate date searches
        val convertDateSearchPattern = ConvertDateSearchPatternUseCase()
        val startLocalDates = mutableListOf<LocalDate>()
        afterDates?.forEach { dateString ->
            val convertedDate = convertDateSearchPattern(dateString)
            if (convertedDate == null) return Result.Invalid
            else startLocalDates.add(convertedDate)
        }
        val getLatestStartDate = GetLatestDateUseCase()
        val latestStartDate = getLatestStartDate(startLocalDates)
        val endLocalDates = mutableListOf<LocalDate>()
        beforeDates?.forEach { dateString ->
            val convertedDate = convertDateSearchPattern(dateString)
            if (convertedDate == null) return Result.Invalid
            else endLocalDates.add(convertedDate)
        }
        val getEarliestEndDate = GetEarliestDateUseCase()
        val earliestEndDate = getEarliestEndDate(endLocalDates)

        // check that start-bound < end-bound; if not, return empty list
        if ((latestStartDate!=null && earliestEndDate!=null) && (latestStartDate >= earliestEndDate)) return Result.Invalid

        // convert to OffsetDateTime and return pair
        val zoneId = ZoneId.systemDefault()
        val startOffsetDateTime =
            if (latestStartDate!=null) ZonedDateTime.of(latestStartDate, LocalTime.of(0,0,0,0), zoneId).toOffsetDateTime()
            else null
        val endOffsetDateTime =
            if (earliestEndDate!=null) ZonedDateTime.of(earliestEndDate, LocalTime.of(0,0,0,0), zoneId).toOffsetDateTime()
            else null

        return if (startOffsetDateTime!=null && endOffsetDateTime==null) Result.BoundingStartSearchDate(startDate=startOffsetDateTime)
        else if (startOffsetDateTime==null && endOffsetDateTime!=null) Result.BoundingEndSearchDate(endDate=endOffsetDateTime)
        else if (startOffsetDateTime!=null && endOffsetDateTime!=null) Result.BoundingSearchDates(startDate=startOffsetDateTime, endDate=endOffsetDateTime)
        else Result.NoBoundingSearchDates
    }

    sealed class Result {
        data object Invalid: Result()
        data object NoBoundingSearchDates: Result()
        data class BoundingSearchDates(val startDate: OffsetDateTime, val endDate: OffsetDateTime): Result()
        data class BoundingStartSearchDate(val startDate: OffsetDateTime): Result()
        data class BoundingEndSearchDate(val endDate: OffsetDateTime): Result()
    }
}