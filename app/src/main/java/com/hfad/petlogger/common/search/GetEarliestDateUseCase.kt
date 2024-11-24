package com.hfad.petlogger.common.search

import java.time.LocalDate

class GetEarliestDateUseCase {
    operator fun invoke(dates: List<LocalDate>): LocalDate? {
        if (dates.isNotEmpty()) {
            var minDate = dates[0]
            for (date in dates) {
                if (date < minDate) {
                    minDate = date
                }
            }
            return minDate
        }
        return null
    }
}