package com.hfad.petlogger.common.usecases

class GetPossessiveFormUseCase {
    operator fun invoke(word: String): String {
        return if (word.endsWith("s", ignoreCase = true)) "$word\'" else "$word's"
    }
}