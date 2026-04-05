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
            val normalizedPath = normalizeConfigCenterPath(item.path)
            val value = item.value
            if (normalizedPath.isNotBlank() && value != null) {
                values[normalizedPath] = value
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
    values.forEach { (path, value) ->
        val normalizedPath = normalizeConfigCenterPath(path)
        if (normalizedPath.isNotBlank()) {
            snapshot[normalizedPath] = value
        }
    }
    return ConfigCenterEnv(
        stringReader = { path ->
            snapshot[normalizeConfigCenterPath(path)]
        },
        listReader = { path ->
            snapshot[normalizeConfigCenterPath(path)]?.toConfigCenterListOrNull()
        },
        mapReader = { path ->
            val normalizedPath = normalizeConfigCenterPath(path)
            val prefix = if (normalizedPath.isBlank()) {
                ""
            } else {
                "$normalizedPath."
            }
            val nested = snapshot.entries
                .filter { (candidatePath, _) -> candidatePath.startsWith(prefix) }
                .associate { (candidatePath, value) ->
                    candidatePath.removePrefix(prefix) to value
                }
                .filterKeys(String::isNotBlank)
            nested.takeIf(Map<String, String>::isNotEmpty)
        },
        keysReader = { path ->
            val normalizedPath = normalizeConfigCenterPath(path)
            val prefix = if (normalizedPath.isBlank()) {
                ""
            } else {
                "$normalizedPath."
            }
            snapshot.keys
                .asSequence()
                .filter { candidatePath -> candidatePath.startsWith(prefix) }
                .map { candidatePath ->
                    candidatePath.removePrefix(prefix)
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
        path: String,
    ): String {
        resolved[path]?.let { value ->
            return value
        }
        check(visiting.add(path)) {
            "配置中心变量存在循环引用: ${visiting.joinToString(" -> ")} -> $path"
        }
        val rawValue = values[path]
            ?: error("配置中心变量引用缺少 path=$path")
        val resolvedValue = CONFIG_CENTER_PLACEHOLDER_REGEX.replace(rawValue) { match ->
            val referencedPath = normalizeConfigCenterPath(match.groupValues[1])
            require(referencedPath.isNotBlank()) {
                "配置中心变量占位符不能为空: $rawValue"
            }
            resolve(referencedPath)
        }
        visiting.remove(path)
        resolved[path] = resolvedValue
        return resolvedValue
    }

    values.keys
        .map(::normalizeConfigCenterPath)
        .filter(String::isNotBlank)
        .forEach(::resolve)
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
