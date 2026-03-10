package site.addzero.notes.server

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

internal object NotesEnv {
    private val initialized = AtomicBoolean(false)
    private val dotEnvValues by lazy { loadDotEnvValues() }
    private val dotEnvWithAliases by lazy { applyAliases(dotEnvValues) }

    fun initialize() {
        if (!initialized.compareAndSet(false, true)) {
            return
        }

        dotEnvWithAliases.forEach { (key, value) ->
            if (value.isBlank()) {
                return@forEach
            }

            val hasProcessEnv = !System.getenv(key).isNullOrBlank()
            val hasSystemProperty = !System.getProperty(key).isNullOrBlank()
            if (!hasProcessEnv && !hasSystemProperty) {
                System.setProperty(key, value)
            }
        }
    }

    fun read(vararg keys: String): String? {
        initialize()

        keys.forEach { key ->
            val fromEnv = System.getenv(key)?.trim().orEmpty()
            if (fromEnv.isNotBlank()) {
                return fromEnv
            }

            val fromProperty = System.getProperty(key)?.trim().orEmpty()
            if (fromProperty.isNotBlank()) {
                return fromProperty
            }

            val fromDotEnv = dotEnvWithAliases[key]?.trim().orEmpty()
            if (fromDotEnv.isNotBlank()) {
                return fromDotEnv
            }
        }

        return null
    }

    private fun resolveDotEnvFile(): File? {
        val explicit = readExplicitEnvFilePath()
        if (explicit.isNotBlank()) {
            val file = File(explicit).absoluteFile
            if (file.isFile) {
                return file
            }
        }

        val cwd = File(System.getProperty("user.dir")).absoluteFile
        val candidates = listOf(
            File(cwd, ".env"),
            File(cwd, "../.env"),
            File(cwd, "apps/notes/.env"),
            File(cwd, "../apps/notes/.env"),
            File(cwd, "../../apps/notes/.env"),
        )

        return candidates
            .map { it.absoluteFile.normalize() }
            .firstOrNull { it.isFile }
    }

    private fun readExplicitEnvFilePath(): String {
        val fromEnv = System.getenv(KEY_ENV_FILE)?.trim().orEmpty()
        if (fromEnv.isNotBlank()) {
            return fromEnv
        }
        return System.getProperty(KEY_ENV_FILE)?.trim().orEmpty()
    }

    private fun loadDotEnvValues(): Map<String, String> {
        val envFile = resolveDotEnvFile() ?: return emptyMap()
        if (!envFile.isFile) {
            return emptyMap()
        }

        val values = linkedMapOf<String, String>()
        envFile.forEachLine { raw ->
            val line = raw.trim()
            if (line.isBlank() || line.startsWith("#") || line.startsWith("//")) {
                return@forEachLine
            }

            val equalsIndex = line.indexOf('=')
            if (equalsIndex <= 0) {
                return@forEachLine
            }

            val key = line.substring(0, equalsIndex).trim()
            if (key.isBlank()) {
                return@forEachLine
            }

            val rawValue = line.substring(equalsIndex + 1).trim()
            val unquoted = unquote(rawValue)
            val expanded = expandValue(unquoted, values)
            values[key] = expanded
        }

        return values
    }

    private fun unquote(value: String): String {
        if (value.length < 2) {
            return value
        }

        val first = value.first()
        val last = value.last()
        return if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            value.substring(1, value.length - 1)
        } else {
            value
        }
    }

    private fun expandValue(value: String, resolvedValues: Map<String, String>): String {
        if (value.isBlank()) {
            return value
        }

        return tokenPattern.replace(value) { match ->
            val token = match.groups[1]?.value ?: match.groups[2]?.value.orEmpty()
            val resolved = resolvedValues[token]
                ?: System.getenv(token)
                ?: System.getProperty(token)

            if (resolved.isNullOrBlank()) {
                match.value
            } else {
                resolved
            }
        }
    }

    private fun applyAliases(values: Map<String, String>): Map<String, String> {
        if (values.isEmpty()) {
            return emptyMap()
        }

        val merged = values.toMutableMap()

        aliasIfMissing(
            merged = merged,
            target = "SERVER_HOST",
            source = "BASE_HOST"
        )
        aliasIfMissing(
            merged = merged,
            target = "SERVER_PORT",
            source = "BASE_PORT"
        )
        aliasIfMissing(
            merged = merged,
            target = "NOTES_API_BASE_URL",
            source = "BASE_URL"
        )

        return merged
    }

    private fun aliasIfMissing(
        merged: MutableMap<String, String>,
        target: String,
        source: String
    ) {
        if (merged[target].isNullOrBlank()) {
            val sourceValue = merged[source]?.trim().orEmpty()
            if (sourceValue.isNotBlank()) {
                merged[target] = sourceValue
            }
        }
    }

    private const val KEY_ENV_FILE = "NOTES_SERVER_ENV_FILE"
    private val tokenPattern = Regex("""\$\{([A-Za-z_][A-Za-z0-9_]*)}|\$([A-Za-z_][A-Za-z0-9_]*)""")
}
