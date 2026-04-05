package site.addzero.configcenter

fun configCenterEnv(
    stringReader: (String) -> String?,
    listReader: (String) -> List<String>?,
    mapReader: (String) -> Map<String, String>?,
    keysReader: (String) -> Set<String>,
): ConfigCenterEnv {
    return ConfigCenterEnv(
        stringReader = stringReader,
        listReader = listReader,
        mapReader = mapReader,
        keysReader = keysReader,
    )
}

class ConfigCenterEnv internal constructor(
    private val stringReader: (String) -> String?,
    private val listReader: (String) -> List<String>?,
    private val mapReader: (String) -> Map<String, String>?,
    private val keysReader: (String) -> Set<String>,
) {
    fun path(
        vararg segments: String,
    ): ConfigCenterScopedEnv {
        return ConfigCenterScopedEnv(
            env = this,
            prefix = segments.asList().joinConfigPath(),
        )
    }

    fun string(
        definition: ConfigCenterKeyDefinition,
    ): String? {
        return string(
            key = definition.key,
            defaultValue = definition.defaultValue,
        )
    }

    fun string(
        key: String,
        defaultValue: String? = null,
    ): String? {
        val normalizedKey = key.trim()
        if (normalizedKey.isBlank()) {
            return defaultValue
        }
        return stringReader(normalizedKey) ?: defaultValue
    }

    fun int(
        definition: ConfigCenterKeyDefinition,
    ): Int? {
        return int(
            key = definition.key,
            defaultValue = definition.defaultValue?.toIntOrNull(),
        )
    }

    fun int(
        key: String,
        defaultValue: Int? = null,
    ): Int? {
        return string(key)?.toIntOrNull() ?: defaultValue
    }

    fun boolean(
        definition: ConfigCenterKeyDefinition,
    ): Boolean? {
        return boolean(
            key = definition.key,
            defaultValue = definition.defaultValue.toBooleanStrictOrNullSafe(),
        )
    }

    fun boolean(
        key: String,
        defaultValue: Boolean? = null,
    ): Boolean? {
        return string(key)?.toBooleanStrictOrNullSafe() ?: defaultValue
    }

    fun list(
        definition: ConfigCenterKeyDefinition,
    ): List<String>? {
        return list(
            key = definition.key,
            defaultValue = definition.defaultValue.parseStringListOrNull(),
        )
    }

    fun list(
        key: String,
        defaultValue: List<String>? = null,
    ): List<String>? {
        val normalizedKey = key.trim()
        if (normalizedKey.isBlank()) {
            return defaultValue
        }
        return listReader(normalizedKey) ?: defaultValue
    }

    fun map(
        path: String,
    ): Map<String, String>? {
        val normalizedPath = path.trim()
        if (normalizedPath.isBlank()) {
            return null
        }
        return mapReader(normalizedPath)
    }

    fun keys(
        path: String,
    ): Set<String> {
        val normalizedPath = path.trim()
        if (normalizedPath.isBlank()) {
            return emptySet()
        }
        return keysReader(normalizedPath)
    }
}

class ConfigCenterScopedEnv internal constructor(
    private val env: ConfigCenterEnv,
    val prefix: String,
) {
    fun child(
        vararg segments: String,
    ): ConfigCenterScopedEnv {
        return ConfigCenterScopedEnv(
            env = env,
            prefix = listOf(prefix, segments.asList().joinConfigPath())
                .joinConfigPath(),
        )
    }

    fun string(
        key: String,
        defaultValue: String? = null,
    ): String? {
        return env.string(
            key = composeKey(key),
            defaultValue = defaultValue,
        )
    }

    fun int(
        key: String,
        defaultValue: Int? = null,
    ): Int? {
        return env.int(
            key = composeKey(key),
            defaultValue = defaultValue,
        )
    }

    fun boolean(
        key: String,
        defaultValue: Boolean? = null,
    ): Boolean? {
        return env.boolean(
            key = composeKey(key),
            defaultValue = defaultValue,
        )
    }

    fun list(
        key: String,
        defaultValue: List<String>? = null,
    ): List<String>? {
        return env.list(
            key = composeKey(key),
            defaultValue = defaultValue,
        )
    }

    fun map(
        key: String? = null,
    ): Map<String, String>? {
        return env.map(composeKey(key))
    }

    fun keys(
        key: String? = null,
    ): Set<String> {
        return env.keys(composeKey(key))
    }

    private fun composeKey(
        key: String?,
    ): String {
        return listOf(prefix, key.orEmpty())
            .joinConfigPath()
    }
}

private fun String?.toBooleanStrictOrNullSafe(): Boolean? {
    return this
        ?.trim()
        ?.lowercase()
        ?.let { value ->
            when (value) {
                "true" -> true
                "false" -> false
                else -> null
            }
        }
}

private fun String?.parseStringListOrNull(): List<String>? {
    val normalized = this?.trim()?.takeIf(String::isNotBlank) ?: return null
    if (normalized.startsWith("[") && normalized.endsWith("]")) {
        return normalized
            .removePrefix("[")
            .removeSuffix("]")
            .split(',')
            .map { part -> part.trim().trim('"') }
            .filter(String::isNotBlank)
    }
    return normalized
        .split(',')
        .map(String::trim)
        .filter(String::isNotBlank)
        .takeIf(List<String>::isNotEmpty)
}

private fun List<String>.joinConfigPath(): String {
    return asSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .joinToString(".")
}
