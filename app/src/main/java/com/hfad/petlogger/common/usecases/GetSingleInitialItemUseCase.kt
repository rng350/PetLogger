package com.hfad.petlogger.common.usecases

// For SingleSelectionTracker's initial selection, for type safety
sealed class GetSingleInitialItemUseCase<T> {
    data class PreExisting<T>(val useCase: GetSingleItemUseCase<T>): GetSingleInitialItemUseCase<T>()
    data class New<T>(val useCase: GetSingleItemUseCase<T>): GetSingleInitialItemUseCase<T>()
}