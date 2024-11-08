package com.hfad.petlogger.common.usecases

interface GetSingleItemUseCase<T> {
    suspend operator fun invoke(): T?
}