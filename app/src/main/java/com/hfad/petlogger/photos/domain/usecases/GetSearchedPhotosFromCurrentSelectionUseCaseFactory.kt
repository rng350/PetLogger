package com.hfad.petlogger.photos.domain.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.usecases.factories.GetSearchedCurrentSelectionUseCaseFactory
import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.photos.data.PhotoDao

class GetSearchedPhotosFromCurrentSelectionUseCaseFactory(private val photoDao: PhotoDao):
    GetSearchedCurrentSelectionUseCaseFactory<Photo> {
    override fun createGetSearchedCurrentSelectionUseCase(currentSelection: LiveData<List<Photo>>): GetSearchedItemsUseCase<Photo> {
        return GetSearchedPhotosFromCurrentSelectionUseCase(photoDao, currentSelection)
    }
}