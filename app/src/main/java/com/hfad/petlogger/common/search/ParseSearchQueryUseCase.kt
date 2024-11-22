package com.hfad.petlogger.common.search

// At instantiation, prefixes must be provided. Do not include colons (":")
// i.e. ["before", "after", "tag"] -- Something like that is good
class ParseSearchQueryUseCase(private val prefixes: List<String> = listOf()) {
    private val prefixRegex: String = prefixes.joinToString(separator = "") { "\\b${it}:|" }
    private val prefixPatternInLineRegex: String = prefixes.joinToString(separator = "") { "${it}:|" }

    operator fun invoke(query: String): Map<String?, List<String>> {
        val quotedMatches = mutableMapOf<String?, MutableList<String>>()

        // Step 1: Parse through any tokens with quoted strings
        val contextPattern = Regex("(?:${prefixRegex}#)?(\".*?\")")
        contextPattern.findAll(query).forEach { matchResult ->
            var prefix: String? = null
            for (item in prefixes) {
                if (matchResult.value.startsWith("${item}:")) {
                    prefix = item
                    break
                }
            }
            if (matchResult.value.startsWith("#")) {
                prefix = "#"
            }
            val content = matchResult.groups[1]?.value ?: ""
            quotedMatches.getOrPut(prefix){ mutableListOf() }.add(content)
        }

        // Step 2: Remove all quoted strings from the query
        val strippedInput = query.replace(contextPattern, "").trim()

        // Step 3: Split the remaining query into tokens
        val tokens = strippedInput.split("\\s+".toRegex()).filter { it.isNotBlank() }

        // Step 4: Parse tokens, handling prefixes with conditions
        val prefixPatternInline = Regex("^(${prefixPatternInLineRegex}#)")

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (prefixPatternInline.containsMatchIn(token)) {
                // Handle case where a prefix is detected
                var prefix = prefixPatternInline.find(token)!!.value // Extract prefix
                val value = token.removePrefix(prefix)
                prefix = prefix.removeSuffix(":")

                if (value.isNotEmpty()) {
                    // Token has an inline value
                    quotedMatches.getOrPut(prefix){ mutableListOf() }.add(value)
                } else if (i + 1 < tokens.size && tokens[i + 1].startsWith("\"")) {
                    // Handle standalone quoted value
                    val nextQuotedMatch = quotedMatches[null]?.find { it == tokens[i + 1].trim('"') }
                    if (nextQuotedMatch != null) {
                        quotedMatches.getOrPut(null){ mutableListOf()}.add(nextQuotedMatch)
                        i++ // Skip the next token as it is consumed
                    }
                }
            }
            else {
                // Handle unbounded terms
                quotedMatches.getOrPut(null){ mutableListOf() }.add(token)
            }
            i++
        }
        return quotedMatches.toMap()
    }
}