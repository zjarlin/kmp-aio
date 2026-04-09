package site.addzero.kcloud.plugins.codegencontext.context

internal expect object CodegenIdentifierTransliterator {
    fun romanize(source: String): String
}

internal fun String.toGeneratedMethodName(defaultName: String = "generatedMethod"): String {
    return identifierWords(defaultName).toLowerCamelIdentifier(defaultName)
}

internal fun String.toGeneratedPropertyName(defaultName: String = "fieldValue"): String {
    return identifierWords(defaultName).toLowerCamelIdentifier(defaultName)
}

internal fun String.toGeneratedTypeName(defaultName: String = "GeneratedModel"): String {
    return identifierWords(defaultName).toUpperCamelIdentifier(defaultName)
}

private fun String.identifierWords(defaultName: String): List<String> {
    val romanized = CodegenIdentifierTransliterator.romanize(trim())
    val words = romanized.toIdentifierWords()
    if (words.isNotEmpty()) {
        return words
    }
    return defaultName.toIdentifierWords().ifEmpty { listOf(defaultName.lowercase()) }
}

private fun String.toIdentifierWords(): List<String> {
    return replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
        .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1 $2")
        .replace(Regex("[^A-Za-z0-9]+"), " ")
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .map { it.lowercase() }
}

private fun List<String>.toLowerCamelIdentifier(defaultName: String): String {
    val candidate =
        if (isEmpty()) {
            defaultName
        } else {
            first() + drop(1).joinToString(separator = "") { word -> word.capitalizeAscii() }
        }
    return candidate.ensureIdentifierPrefix(defaultName)
}

private fun List<String>.toUpperCamelIdentifier(defaultName: String): String {
    val candidate =
        if (isEmpty()) {
            defaultName
        } else {
            joinToString(separator = "") { word -> word.capitalizeAscii() }
        }
    return candidate.ensureIdentifierPrefix(defaultName)
}

private fun String.capitalizeAscii(): String {
    return replaceFirstChar { char ->
        if (char.isLowerCase()) {
            char.titlecase()
        } else {
            char.toString()
        }
    }
}

private fun String.ensureIdentifierPrefix(defaultName: String): String {
    val candidate = if (isBlank()) defaultName else this
    return if (candidate.first().isDigit()) {
        "_$candidate"
    } else {
        candidate
    }
}
