package com.hfad.petlogger.photodisplay

import kotlinx.coroutines.flow.Flow

interface GetAssociatedItemsUseCase<T> {
    operator fun invoke(): Flow<List<T>>
}