package com.hfad.petlogger.common.usecases

interface GetItemsUseCase<T> {
    val onLastPage: Boolean
    suspend operator fun invoke(): List<T>
    fun resetCurrentPoint()
}