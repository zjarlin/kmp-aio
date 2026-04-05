package site.addzero.configcenter

import org.koin.core.annotation.Single

data class ConfigCenterScope(
    val namespace: String,
    val active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
) {
    val normalizedNamespace: String = normalizeConfigCenterNamespace(namespace)
    val normalizedActive: String = normalizeConfigCenterActive(active)
}

@Single
class ConfigCenterBeanFactory(
    private val configCenterValueService: ConfigCenterValueService,
) {
    fun env(
        namespace: String,
        active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    ): ConfigCenterEnv {
        val scope = ConfigCenterScope(
            namespace = namespace,
            active = active,
        )
        val values = LinkedHashMap<String, String>()
        configCenterValueService.listValues(
            namespace = scope.normalizedNamespace,
            active = scope.normalizedActive,
            limit = Int.MAX_VALUE,
        ).forEach { item ->
            val key = item.key.trim()
            val value = item.value?.trim()
            if (key.isNotBlank() && value != null) {
                values[key] = value
            }
        }
        return configCenterEnvOf(resolveConfigCenterSnapshot(values))
    }

    companion object
}

fun configCenterEnvOf(
    values: Map<String, String>,
): ConfigCenterEnv {
    val snapshot = LinkedHashMap<String, String>()
    values.forEach { (key, value) ->
        val normalizedKey = key.trim()
        if (normalizedKey.isNotBlank()) {
            snapshot[normalizedKey] = value
        }
    }
    return ConfigCenterEnv(
        stringReader = { key ->
            snapshot[key.trim()]
        },
        listReader = { key ->
            snapshot[key.trim()]?.toConfigCenterListOrNull()
        },
        mapReader = { path ->
            val normalizedPath = path.trim().removeSuffix(".")
            val prefix = if (normalizedPath.isBlank()) {
                ""
            } else {
                "$normalizedPath."
            }
            val nested = snapshot.entries
                .filter { (key, _) ->
                    key.startsWith(prefix)
                }
                .associate { (key, value) ->
                    key.removePrefix(prefix) to value
                }
                .filterKeys(String::isNotBlank)
            nested.takeIf(Map<String, String>::isNotEmpty)
        },
        keysReader = { path ->
            val normalizedPath = path.trim().removeSuffix(".")
            val prefix = if (normalizedPath.isBlank()) {
                ""
            } else {
                "$normalizedPath."
            }
            snapshot.keys
                .asSequence()
                .filter { key -> key.startsWith(prefix) }
                .map { key ->
                    key.removePrefix(prefix)
                        .substringBefore('.')
                        .trim()
                }
                .filter(String::isNotBlank)
                .toSet()
        },
    )
}

fun resolveConfigCenterSnapshot(
    values: Map<String, String>,
): Map<String, String> {
    val resolved = LinkedHashMap<String, String>()
    val visiting = LinkedHashSet<String>()

    fun resolve(
        key: String,
    ): String {
        resolved[key]?.let { value ->
            return value
        }
        check(visiting.add(key)) {
            "配置中心变量存在循环引用: ${visiting.joinToString(" -> ")} -> $key"
        }
        val rawValue = values[key]
            ?: error("配置中心变量引用缺少 key=$key")
        val resolvedValue = CONFIG_CENTER_PLACEHOLDER_REGEX.replace(rawValue) { match ->
            val referencedKey = match.groupValues[1].trim()
            require(referencedKey.isNotBlank()) {
                "配置中心变量占位符不能为空: $rawValue"
            }
            resolve(referencedKey)
        }
        visiting.remove(key)
        resolved[key] = resolvedValue
        return resolvedValue
    }

    values.keys.forEach(::resolve)
    return resolved
}

private fun String.toConfigCenterListOrNull(): List<String>? {
    val normalized = trim()
    if (normalized.isBlank()) {
        return null
    }
    if (normalized.startsWith("[") && normalized.endsWith("]")) {
        return normalized
            .removePrefix("[")
            .removeSuffix("]")
            .split(',')
            .map { part -> part.trim().trim('"') }
            .filter(String::isNotBlank)
            .takeIf(List<String>::isNotEmpty)
    }
    return normalized
        .split(',')
        .map(String::trim)
        .filter(String::isNotBlank)
        .takeIf(List<String>::isNotEmpty)
}

private val CONFIG_CENTER_PLACEHOLDER_REGEX =
    Regex("\\$\\{([A-Za-z0-9._-]+)}")
