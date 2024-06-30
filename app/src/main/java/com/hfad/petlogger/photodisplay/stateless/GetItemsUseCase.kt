package com.hfad.petlogger.photodisplay.stateless

interface GetItemsUseCase<T> {
    suspend operator fun invoke(): List<T>
}