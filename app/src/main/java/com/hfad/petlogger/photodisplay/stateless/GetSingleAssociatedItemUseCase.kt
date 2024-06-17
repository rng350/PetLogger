package com.hfad.petlogger.photodisplay.stateless

interface GetSingleAssociatedItemUseCase<T> {
    suspend operator fun invoke(): T?
}