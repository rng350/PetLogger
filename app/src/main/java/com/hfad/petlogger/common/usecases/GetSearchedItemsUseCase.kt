package com.hfad.petlogger.common.usecases

import com.hfad.petlogger.common.search.SanitizeSearchQueryUseCase

interface GetSearchedItemsUseCase<T>: GetItemsUseCase<T> {
    var currentQuery: String
    override val onLastPage: Boolean
    override suspend operator fun invoke(): List<T>

    fun changeSearchQuery(query: String) {
        val sanitizeSearchQuery = SanitizeSearchQueryUseCase()
        currentQuery = sanitizeSearchQuery(query)
        resetCurrentPoint()
    }
    override fun resetCurrentPoint()
}