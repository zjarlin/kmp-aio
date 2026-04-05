package site.addzero.configcenter

import io.ktor.server.config.ApplicationConfig

fun ApplicationConfig.stringOrNull(
    definition: ConfigCenterKeyDefinition,
): String? {
    return propertyOrNull(definition.key)?.getString()
}

fun ApplicationConfig.string(
    definition: ConfigCenterKeyDefinition,
): String? {
    return stringOrNull(definition) ?: definition.defaultValue
}

fun ApplicationConfig.intOrNull(
    definition: ConfigCenterKeyDefinition,
): Int? {
    return stringOrNull(definition)?.toIntOrNull()
}

fun ApplicationConfig.int(
    definition: ConfigCenterKeyDefinition,
): Int? {
    return intOrNull(definition) ?: definition.defaultValue?.toIntOrNull()
}

fun ApplicationConfig.booleanOrNull(
    definition: ConfigCenterKeyDefinition,
): Boolean? {
    return stringOrNull(definition)?.trim()?.lowercase()?.let { value ->
        when (value) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }
}

fun ApplicationConfig.boolean(
    definition: ConfigCenterKeyDefinition,
): Boolean? {
    return booleanOrNull(definition) ?: definition.defaultValue?.trim()?.lowercase()?.let { value ->
        when (value) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }
}

fun ApplicationConfig.listOrNull(
    definition: ConfigCenterKeyDefinition,
): List<String>? {
    return propertyOrNull(definition.key)?.getList()
}
