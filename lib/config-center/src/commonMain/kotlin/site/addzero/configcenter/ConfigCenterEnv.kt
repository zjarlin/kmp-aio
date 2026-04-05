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
        path: String,
        defaultValue: String? = null,
    ): String? {
        val normalizedPath = normalizeConfigCenterPath(path)
        if (normalizedPath.isBlank()) {
            return defaultValue
        }
        return stringReader(normalizedPath) ?: defaultValue
    }

    fun int(
        path: String,
        defaultValue: Int? = null,
    ): Int? {
        return string(path)?.toIntOrNull() ?: defaultValue
    }

    fun boolean(
        path: String,
        defaultValue: Boolean? = null,
    ): Boolean? {
        return string(path)?.toBooleanStrictOrNullSafe() ?: defaultValue
    }

    fun list(
        path: String,
        defaultValue: List<String>? = null,
    ): List<String>? {
        val normalizedPath = normalizeConfigCenterPath(path)
        if (normalizedPath.isBlank()) {
            return defaultValue
        }
        return listReader(normalizedPath) ?: defaultValue
    }

    fun map(
        path: String,
    ): Map<String, String>? {
        val normalizedPath = normalizeConfigCenterPath(path)
        if (normalizedPath.isBlank()) {
            return null
        }
        return mapReader(normalizedPath)
    }

    fun keys(
        path: String,
    ): Set<String> {
        val normalizedPath = normalizeConfigCenterPath(path)
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
        path: String,
        defaultValue: String? = null,
    ): String? {
        return env.string(
            path = composePath(path),
            defaultValue = defaultValue,
        )
    }

    fun int(
        path: String,
        defaultValue: Int? = null,
    ): Int? {
        return env.int(
            path = composePath(path),
            defaultValue = defaultValue,
        )
    }

    fun boolean(
        path: String,
        defaultValue: Boolean? = null,
    ): Boolean? {
        return env.boolean(
            path = composePath(path),
            defaultValue = defaultValue,
        )
    }

    fun list(
        path: String,
        defaultValue: List<String>? = null,
    ): List<String>? {
        return env.list(
            path = composePath(path),
            defaultValue = defaultValue,
        )
    }

    fun map(
        path: String? = null,
    ): Map<String, String>? {
        return env.map(composePath(path))
    }

    fun keys(
        path: String? = null,
    ): Set<String> {
        return env.keys(composePath(path))
    }

    private fun composePath(
        path: String?,
    ): String {
        return listOf(prefix, path.orEmpty())
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

private fun List<String>.joinConfigPath(): String {
    return asSequence()
        .map(::normalizeConfigCenterPath)
        .filter(String::isNotBlank)
        .joinToString(".")
}
