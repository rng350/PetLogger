package com.hfad.petlogger.common.usecases

// For MultiSelectionTracker's initial selection, for type safety
sealed class GetMultipleInitialItemsUseCase<T> {
    data class PreExisting<T>(val useCase: GetItemsUseCase<T>): GetMultipleInitialItemsUseCase<T>()
    data class New<T>(val useCase: GetSingleItemUseCase<T>): GetMultipleInitialItemsUseCase<T>()
}