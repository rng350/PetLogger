package com.hfad.petlogger.common.usecases

interface GetSearchedItemsUseCase<T>: GetItemsUseCase<T> {
    var currentQuery: String
    override val onLastPage: Boolean
    override suspend operator fun invoke(): List<T>

    fun changeSearchQuery(query: String) {
        currentQuery = query
        resetCurrentPoint()
    }
    override fun resetCurrentPoint()
}