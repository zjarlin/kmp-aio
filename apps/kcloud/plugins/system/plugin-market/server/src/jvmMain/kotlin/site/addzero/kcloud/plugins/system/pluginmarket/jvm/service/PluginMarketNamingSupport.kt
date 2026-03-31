package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

internal data class PresetFile(
    val path: String,
    val content: String,
    val group: String,
)

internal fun String.toPascalCase(): String {
    return split("-", "_", ".", "/")
        .filter { it.isNotBlank() }
        .joinToString("") { part -> part.replaceFirstChar { it.uppercase() } }
}

internal fun String.camelCase(): String {
    val pascal = toPascalCase()
    return pascal.replaceFirstChar { it.lowercase() }
}

internal fun String.appendSegment(segment: String): String {
    return if (isBlank()) segment else "$this.$segment"
}

internal fun String.toPath(): String = replace(".", "/")
