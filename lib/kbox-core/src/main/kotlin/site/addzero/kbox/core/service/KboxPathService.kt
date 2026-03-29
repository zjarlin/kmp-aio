package site.addzero.kbox.core.service

import org.koin.core.annotation.Single
import site.addzero.kbox.core.KBOX_APP_NAME
import site.addzero.kbox.core.model.KboxInstallerPlatform
import site.addzero.kbox.core.model.KboxInstallerRule
import site.addzero.kbox.core.support.normalizeSegments
import site.addzero.kbox.core.support.sanitizeFileName
import site.addzero.kbox.core.support.stableShortHash
import java.io.File

@Single
open class KboxPathService {
    open fun appDataDir(): File {
        val userHome = File(requiredSystemProperty("user.home"))
        val osName = System.getProperty("os.name").orEmpty()
        val resolved = when {
            osName.contains("Mac", ignoreCase = true) -> {
                File(userHome, "Library/Application Support/$KBOX_APP_NAME")
            }

            osName.contains("Windows", ignoreCase = true) -> {
                val baseDir = System.getenv("LOCALAPPDATA")
                    ?.takeIf { it.isNotBlank() }
                    ?: System.getenv("APPDATA")?.takeIf { it.isNotBlank() }
                    ?: userHome.absolutePath
                File(baseDir, KBOX_APP_NAME)
            }

            else -> {
                val baseDir = System.getenv("XDG_DATA_HOME")
                    ?.takeIf { it.isNotBlank() }
                    ?: File(userHome, ".local/share").absolutePath
                File(baseDir, KBOX_APP_NAME)
            }
        }
        return resolved.apply { mkdirs() }
    }

    fun settingsFile(): File {
        return File(configDir(), "settings.json")
    }

    fun installerHistoryFile(): File {
        return File(historyDir(), "installer-history.json")
    }

    fun offloadHistoryFile(): File {
        return File(historyDir(), "offload-history.json")
    }

    fun packageTarget(
        rule: KboxInstallerRule,
        sourceFile: File,
    ): File {
        val platformDir = when (rule.platform) {
            KboxInstallerPlatform.WINDOWS -> "windows"
            KboxInstallerPlatform.MACOS -> "macos"
            KboxInstallerPlatform.LINUX -> "linux"
            KboxInstallerPlatform.ANDROID -> "android"
            KboxInstallerPlatform.IOS -> "ios"
            KboxInstallerPlatform.OTHER -> "other"
        }
        val extension = sourceFile.extension.lowercase()
        val fileName = buildDestinationFileName(sourceFile)
        return File(packagesDir(), "$platformDir/${rule.bucket.ifBlank { extension }}/$fileName")
    }

    fun offloadRelativePath(
        sourceFile: File,
    ): String {
        val appDataRoot = appDataDir().canonicalFile
        val canonicalSource = sourceFile.canonicalFile
        val relativeFromAppData = canonicalSource.toRelativeStringOrNull(appDataRoot)
        if (relativeFromAppData != null) {
            return relativeFromAppData.replace(File.separatorChar, '/')
        }
        return listOf("offload")
            .plus(normalizeSegments(canonicalSource.absolutePath))
            .joinToString("/")
    }

    private fun buildDestinationFileName(
        sourceFile: File,
    ): String {
        val hash = stableShortHash(sourceFile.absolutePath)
        val baseName = sanitizeFileName(sourceFile.nameWithoutExtension)
        val extension = sourceFile.extension.lowercase()
        return if (extension.isBlank()) {
            "${baseName}__${hash}"
        } else {
            "${baseName}__${hash}.${extension}"
        }
    }

    private fun packagesDir(): File {
        return File(appDataDir(), "packages").apply { mkdirs() }
    }

    private fun historyDir(): File {
        return File(appDataDir(), "history").apply { mkdirs() }
    }

    private fun configDir(): File {
        return File(appDataDir(), "config").apply { mkdirs() }
    }

    private fun File.toRelativeStringOrNull(
        baseDir: File,
    ): String? {
        return runCatching {
            val sourcePath = this.toPath().normalize()
            val basePath = baseDir.toPath().normalize()
            if (!sourcePath.startsWith(basePath)) {
                null
            } else {
                basePath.relativize(sourcePath).toString()
            }
        }.getOrNull()
    }

    private fun requiredSystemProperty(
        name: String,
    ): String {
        val value = System.getProperty(name)?.trim()
        check(!value.isNullOrEmpty()) {
            "系统属性缺失：$name"
        }
        return value
    }
}
