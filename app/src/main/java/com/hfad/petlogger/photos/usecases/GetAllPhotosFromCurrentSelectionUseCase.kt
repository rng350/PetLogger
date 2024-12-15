package com.hfad.petlogger.photos.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.Photo

class GetAllPhotosFromCurrentSelectionUseCase(
    val currentSelection: LiveData<List<Photo>>
): GetItemsUseCase<Photo> {
    private var _onLastPage = false
    override val onLastPage: Boolean get() = _onLastPage

    override suspend fun invoke(): List<Photo> {
        return currentSelection.value ?: listOf()
    }

    override fun resetCurrentPoint() {
    }
}