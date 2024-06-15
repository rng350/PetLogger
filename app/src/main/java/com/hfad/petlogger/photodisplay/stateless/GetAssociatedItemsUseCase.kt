package com.hfad.petlogger.photodisplay.stateless

interface GetAssociatedItemsUseCase<T> {
    suspend operator fun invoke(): List<T>
}