package com.hfad.petlogger.photodisplay

import com.hfad.petlogger.entities.Photo
import kotlinx.coroutines.flow.Flow

interface GetAssociatedPhotosUseCase {
    operator fun invoke(): Flow<List<Photo>>
}