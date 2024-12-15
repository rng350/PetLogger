package com.hfad.petlogger.photos.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.factories.GetSearchedCurrentSelectionUseCaseFactory
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.photos.PhotoDao

class GetSearchedPhotosFromCurrentSelectionUseCaseFactory(private val photoDao: PhotoDao):
    GetSearchedCurrentSelectionUseCaseFactory<Photo> {
    override fun createGetSearchedCurrentSelectionUseCase(currentSelection: LiveData<List<Photo>>): GetSearchedItemsUseCase<Photo> {
        return GetSearchedPhotosFromCurrentSelectionUseCase(photoDao, currentSelection)
    }
}