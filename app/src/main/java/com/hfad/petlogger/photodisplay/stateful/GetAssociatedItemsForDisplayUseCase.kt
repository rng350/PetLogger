package com.hfad.petlogger.photodisplay.stateful

import kotlinx.coroutines.flow.Flow

interface GetAssociatedItemsForDisplayUseCase<T> {
    operator fun invoke(): Flow<List<T>>
}