package com.hfad.petlogger.common.usecases

class SanitizeSearchQueryUseCase {
    operator fun invoke(query: String): String {
        var sanitized = query.replace("\"", "\"\"")
        sanitized = sanitized.replace("'", "''")
        sanitized = sanitized.replace("\\", "\\\\")
        val specialTokens = listOf("AND", "OR", "NOT")
        sanitized = sanitized.split(Regex("\\s+")).joinToString(" ") { word ->
            if (word.uppercase() in specialTokens) "\"$word\"" else word
        }
        return sanitized
    }
}