package com.hfad.petlogger.common.search

import java.time.LocalDate

class GetLatestDateUseCase {
    operator fun invoke(dates: List<LocalDate>): LocalDate? {
        if (dates.isNotEmpty()) {
            var maxDate = dates[0]
            for (date in dates) {
                if (date > maxDate) {
                    maxDate = date
                }
            }
            return maxDate
        }
        return null
    }
}