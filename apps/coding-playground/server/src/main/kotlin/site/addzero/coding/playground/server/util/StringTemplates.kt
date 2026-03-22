package site.addzero.coding.playground.server.util

private val bracePattern = Regex("\\{\\{([A-Za-z0-9_]+)}}")

fun renderTemplateString(template: String, variables: Map<String, String>): String {
    return bracePattern.replace(template) { match ->
        val key = match.groupValues[1]
        variables[key]
            ?: throw IllegalArgumentException("Missing template variable '$key' for '$template'")
    }
}

fun String.lowerCamelCase(): String {
    return replaceFirstChar { it.lowercase() }
}
