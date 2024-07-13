package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetAllPhotosForDisplayUseCase(private val mediaRepository: MediaRepository): GetItemsForDisplayUseCase<Photo> {
    override fun invoke(): Flow<List<Photo>> {
        return mediaRepository.getAllPhotosAsFlow()
    }
}