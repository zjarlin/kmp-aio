package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class McuPortRemarkStore private constructor(
    private val json: Json,
    private val storeFile: File?,
) {
    private val lock = Any()
    private var loaded = false
    private val remarks = linkedMapOf<String, String>()

    fun findRemark(
        deviceKey: String?,
    ): String {
        val normalizedKey = deviceKey?.trim().orEmpty()
        if (normalizedKey.isBlank()) {
            return ""
        }
        synchronized(lock) {
            ensureLoaded()
            return remarks[normalizedKey].orEmpty()
        }
    }

    fun updateRemark(
        deviceKey: String,
        remark: String,
    ) {
        val normalizedKey = deviceKey.trim()
        require(normalizedKey.isNotBlank()) { "deviceKey is required" }
        val normalizedRemark = remark.trim()
        synchronized(lock) {
            ensureLoaded()
            if (normalizedRemark.isBlank()) {
                remarks.remove(normalizedKey)
            } else {
                remarks[normalizedKey] = normalizedRemark
            }
            persist()
        }
    }

    private fun ensureLoaded() {
        if (loaded) {
            return
        }
        loaded = true
        val file = storeFile ?: return
        if (!file.exists()) {
            return
        }
        val decoded = runCatching {
            json.decodeFromString<PersistedMcuPortRemarks>(file.readText())
        }.getOrElse { error ->
            throw IllegalStateException("读取串口备注失败: ${file.absolutePath}", error)
        }
        remarks.clear()
        remarks.putAll(decoded.items.filterValues { value -> value.isNotBlank() })
    }

    private fun persist() {
        val file = storeFile ?: return
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            check(parent.mkdirs() || parent.exists()) {
                "无法创建串口备注目录: ${parent.absolutePath}"
            }
        }
        file.writeText(
            json.encodeToString(
                PersistedMcuPortRemarks(
                    items = remarks.toMap(),
                ),
            ),
        )
    }

    companion object {
        fun persisted(
            json: Json,
        ): McuPortRemarkStore {
            return McuPortRemarkStore(
                json = json,
                storeFile = resolveStoreFile(),
            )
        }

        fun inMemory(
            json: Json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            },
        ): McuPortRemarkStore {
            return McuPortRemarkStore(
                json = json,
                storeFile = null,
            )
        }

        private fun resolveStoreFile(): File {
            val userHome = System.getProperty("user.home").orEmpty()
            val osName = System.getProperty("os.name").orEmpty()
            val baseDir = when {
                osName.contains("Mac", ignoreCase = true) -> {
                    File(userHome, "Library/Application Support/KCloud")
                }

                osName.contains("Windows", ignoreCase = true) -> {
                    File(
                        System.getenv("LOCALAPPDATA")
                            ?: System.getenv("APPDATA")
                            ?: userHome,
                        "KCloud",
                    )
                }

                else -> {
                    val xdgDataHome = System.getenv("XDG_DATA_HOME")
                        ?.takeIf { it.isNotBlank() }
                        ?: File(userHome, ".local/share").absolutePath
                    File(xdgDataHome, "kcloud")
                }
            }
            return File(baseDir, "mcu-console/serial-port-remarks.json")
        }
    }
}

@Serializable
private data class PersistedMcuPortRemarks(
    val items: Map<String, String> = emptyMap(),
)
