package com.hfad.petlogger.photodisplay.stateless

interface GetSingleItemUseCase<T> {
    suspend operator fun invoke(): T?
}