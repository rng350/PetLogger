package com.hfad.petlogger.photodisplay.stateful

import kotlinx.coroutines.flow.Flow

interface GetItemsForDisplayUseCase<T> {
    operator fun invoke(): Flow<List<T>>
}