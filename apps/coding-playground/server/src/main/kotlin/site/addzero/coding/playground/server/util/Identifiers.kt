package site.addzero.coding.playground.server.util

fun String.toPascalIdentifier(): String {
    val parts = split(Regex("[^A-Za-z0-9]+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) {
        return "Generated"
    }
    return parts.joinToString("") { part ->
        part.lowercase().replaceFirstChar { it.uppercase() }
    }
}

fun String.toLowerCamelIdentifier(): String {
    val pascal = toPascalIdentifier()
    return pascal.replaceFirstChar { it.lowercase() }
}
