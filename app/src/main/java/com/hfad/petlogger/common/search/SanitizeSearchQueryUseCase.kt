package com.hfad.petlogger.common.search

class SanitizeSearchQueryUseCase {
    operator fun invoke(query: String): String {
        var sanitized = query
        // ensure balanced double quotes for exact phrase search
        val doubleQuoteCount = query.count { it == '"' }
        if (doubleQuoteCount>0 && doubleQuoteCount%2!=0) {
            val lastQuoteIndex = query.lastIndexOf('"')
            val sb = StringBuilder(query)
            sb.setCharAt(lastQuoteIndex, ' ')
            sanitized = sb.toString()
        }
        // escape backslashes (\), as they can cause issues in SQLite
        sanitized = sanitized.replace("\\", "\\\\")
        // ensure special tokens (AND, OR, NOT) are not misinterpreted
        /*val specialTokens = listOf("AND", "OR", "NOT")
        sanitized = sanitized.split(Regex("\\s+")).joinToString(" ") { word ->
            if (word.uppercase() in specialTokens) "\"$word\"" else word
        }*/

        return sanitized
    }

    operator fun invoke(queries: List<String>): List<String> {
        return queries.map { invoke(it) }
    }
}