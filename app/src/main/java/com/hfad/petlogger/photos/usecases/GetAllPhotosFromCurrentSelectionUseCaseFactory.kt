package com.hfad.petlogger.photos.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.factories.GetAllCurrentSelectionUseCaseFactory
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.Photo

class GetAllPhotosFromCurrentSelectionUseCaseFactory: GetAllCurrentSelectionUseCaseFactory<Photo> {
    override fun createGetAllCurrentSelectionUseCase(currentSelection: LiveData<List<Photo>>): GetItemsUseCase<Photo> {
        return GetAllPhotosFromCurrentSelectionUseCase(currentSelection)
    }
}