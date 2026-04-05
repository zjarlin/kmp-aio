package site.addzero.configcenter

import io.ktor.server.config.ApplicationConfig

fun ApplicationConfig.stringOrNull(
    path: String,
): String? {
    return propertyOrNull(path)?.getString()
}

fun ApplicationConfig.string(
    path: String,
    defaultValue: String? = null,
): String? {
    return stringOrNull(path) ?: defaultValue
}

fun ApplicationConfig.intOrNull(
    path: String,
): Int? {
    return stringOrNull(path)?.toIntOrNull()
}

fun ApplicationConfig.int(
    path: String,
    defaultValue: Int? = null,
): Int? {
    return intOrNull(path) ?: defaultValue
}

fun ApplicationConfig.booleanOrNull(
    path: String,
): Boolean? {
    return stringOrNull(path)?.trim()?.lowercase()?.let { value ->
        when (value) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }
}

fun ApplicationConfig.boolean(
    path: String,
    defaultValue: Boolean? = null,
): Boolean? {
    return booleanOrNull(path) ?: defaultValue
}

fun ApplicationConfig.listOrNull(
    path: String,
): List<String>? {
    return propertyOrNull(path)?.getList()
}
