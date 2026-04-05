package site.addzero.kbox.core.service

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.koin.core.annotation.Single
import site.addzero.core.network.json.prettyJson
import site.addzero.kbox.core.KBOX_APP_NAME
import site.addzero.kbox.core.model.KboxInstallerPlatform
import site.addzero.kbox.core.model.KboxInstallerRule
import site.addzero.kbox.core.support.normalizeSegments
import site.addzero.kbox.core.support.sanitizeFileName
import site.addzero.kbox.core.support.stableShortHash
import site.addzero.util.PathUtil
import java.io.File

@Single
open class KboxPathService {
    private val json = prettyJson

    open fun defaultAppDataDir(): File {
        return PathUtil.appDataDir(
            appName = KBOX_APP_NAME,
            createDirectories = false,
        )
    }

    open fun appDataDir(): File {
        return resolveAppDataDir(
            localAppDataOverride = currentAppDataOverride(),
            createDirectories = true,
        )
    }

    fun resolveAppDataDir(
        localAppDataOverride: String,
        createDirectories: Boolean = true,
    ): File {
        val normalizedOverride = normalizeOverride(localAppDataOverride)
        val resolved = if (normalizedOverride.isBlank()) {
            defaultAppDataDir()
        } else {
            File(normalizedOverride)
        }.absoluteFile
        if (createDirectories) {
            resolved.mkdirs()
        }
        return resolved
    }

    fun currentAppDataOverride(): String {
        val locatorFile = appDataLocatorFile()
        if (!locatorFile.isFile) {
            return ""
        }
        return runCatching {
            json.decodeFromString(KboxAppDataLocator.serializer(), locatorFile.readText())
                .localAppDataOverride
                .trim()
        }.getOrDefault("")
    }

    fun writeAppDataOverride(
        localAppDataOverride: String,
    ) {
        val normalizedOverride = normalizeOverride(localAppDataOverride)
        val locatorFile = appDataLocatorFile()
        if (normalizedOverride.isBlank()) {
            locatorFile.delete()
            return
        }
        locatorFile.parentFile?.mkdirs()
        locatorFile.writeText(
            json.encodeToString(
                KboxAppDataLocator(
                    localAppDataOverride = normalizedOverride,
                ),
            ),
        )
    }

    fun settingsFile(): File {
        return settingsFileForAppDataDir(appDataDir())
    }

    fun settingsFileForOverride(
        localAppDataOverride: String,
    ): File {
        return settingsFileForAppDataDir(
            resolveAppDataDir(
                localAppDataOverride = localAppDataOverride,
                createDirectories = true,
            ),
        )
    }

    fun settingsFileForAppDataDir(
        appDataDir: File,
    ): File {
        return File(configDir(appDataDir), "settings.json")
    }

    fun installerHistoryFile(): File {
        return File(historyDir(), "installer-history.json")
    }

    fun offloadHistoryFile(): File {
        return File(historyDir(), "offload-history.json")
    }

    fun syncIndexFile(): File {
        return File(configDir(), "sync-index.json")
    }

    fun trashIndexFile(): File {
        return File(configDir(), "trash-index.json")
    }

    fun dotfileRegistryFile(): File {
        return File(configDir(), "dotfiles.json")
    }

    fun composeProjectsRegistryFile(): File {
        return File(configDir(), "compose-projects.json")
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

    fun packagesDir(): File {
        return packagesDir(appDataDir())
    }

    fun packagesDir(
        appDataDir: File,
    ): File {
        return childDirectory(appDataDir, "packages")
    }

    fun historyDir(): File {
        return historyDir(appDataDir())
    }

    fun historyDir(
        appDataDir: File,
    ): File {
        return childDirectory(appDataDir, "history")
    }

    fun configDir(): File {
        return configDir(appDataDir())
    }

    fun configDir(
        appDataDir: File,
    ): File {
        return childDirectory(appDataDir, "config")
    }

    fun dotfilesDir(): File {
        return dotfilesDir(appDataDir())
    }

    fun dotfilesDir(
        appDataDir: File,
    ): File {
        return childDirectory(appDataDir, "dotfiles")
    }

    fun dotfilesBackupDir(): File {
        return dotfilesBackupDir(appDataDir())
    }

    fun dotfilesBackupDir(
        appDataDir: File,
    ): File {
        return childDirectory(dotfilesDir(appDataDir), "_backup")
    }

    fun packageProfilesDir(): File {
        return packageProfilesDir(appDataDir())
    }

    fun packageProfilesDir(
        appDataDir: File,
    ): File {
        return childDirectory(appDataDir, "package-profiles")
    }

    fun trashDir(): File {
        return trashDir(appDataDir())
    }

    fun trashDir(
        appDataDir: File,
    ): File {
        return childDirectory(appDataDir, "trash")
    }

    fun tempDir(): File {
        return tempDir(appDataDir())
    }

    fun tempDir(
        appDataDir: File,
    ): File {
        return childDirectory(appDataDir, "tmp")
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

    private fun appDataLocatorFile(): File {
        return File(
            PathUtil.userHomeDir().path,
            ".kbox-app-data.json",
        )
    }

    private fun normalizeOverride(
        localAppDataOverride: String,
    ): String {
        val trimmed = localAppDataOverride.trim()
        if (trimmed.isBlank()) {
            return ""
        }
        val userHome = PathUtil.userHomeDir()
        val expanded = if (trimmed == "~") {
            userHome.path
        } else if (trimmed.startsWith("~/")) {
            File(userHome, trimmed.removePrefix("~/")).path
        } else {
            trimmed
        }
        return runCatching {
            File(expanded).absoluteFile.canonicalPath
        }.getOrElse {
            File(expanded).absoluteFile.path
        }
    }

    private fun childDirectory(
        parent: File,
        vararg names: String,
    ): File {
        return PathUtil.child(
            parent,
            *names,
            createDirectories = true,
        )
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

    @Serializable
    private data class KboxAppDataLocator(
        val localAppDataOverride: String = "",
    )
}
