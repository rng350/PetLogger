package com.hfad.petlogger.photodisplay.stateless

interface GetItemsUseCase<T> {
    val onLastPage: Boolean
    suspend operator fun invoke(): List<T>
    fun resetCurrentPoint()
}