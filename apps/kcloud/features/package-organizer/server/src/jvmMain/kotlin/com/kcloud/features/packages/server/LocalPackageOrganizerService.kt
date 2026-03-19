package com.kcloud.features.packages.server

import com.kcloud.feature.KCloudLocalPaths
import com.kcloud.feature.readKCloudJson
import com.kcloud.feature.writeKCloudJson
import com.kcloud.features.packages.ManagedPackageItem
import com.kcloud.features.packages.PackageOrganizeResult
import com.kcloud.features.packages.PackageOrganizerService
import com.kcloud.features.packages.PackageOrganizerSettings
import java.io.File
import java.time.Instant
import org.koin.core.annotation.Single

private const val PACKAGE_FEATURE_ID = "package-organizer"

@Single
class LocalPackageOrganizerService : PackageOrganizerService {
    private val settingsFile = File(KCloudLocalPaths.featureDir(PACKAGE_FEATURE_ID), "settings.json")

    override fun loadSettings(): PackageOrganizerSettings {
        return readKCloudJson(settingsFile) {
            PackageOrganizerSettings(
                scanDirectory = File(System.getProperty("user.home"), "Downloads").absolutePath,
                targetDirectory = File(KCloudLocalPaths.featureDir(PACKAGE_FEATURE_ID), "organized").absolutePath
            )
        }
    }

    override fun saveSettings(settings: PackageOrganizerSettings): PackageOrganizerSettings {
        val normalized = settings.copy(
            scanDirectory = settings.scanDirectory.ifBlank {
                File(System.getProperty("user.home"), "Downloads").absolutePath
            },
            targetDirectory = settings.targetDirectory.ifBlank {
                File(KCloudLocalPaths.featureDir(PACKAGE_FEATURE_ID), "organized").absolutePath
            }
        )
        writeKCloudJson(settingsFile, normalized)
        return normalized
    }

    override fun scanPackages(): List<ManagedPackageItem> {
        val settings = loadSettings()
        val scanDirectory = File(settings.scanDirectory)
        if (!scanDirectory.exists() || !scanDirectory.isDirectory) {
            return emptyList()
        }

        return scanDirectory.walkTopDown()
            .maxDepth(2)
            .filter { file -> file.isFile && categoryFor(file.name) != null }
            .map { file ->
                val extension = extensionFor(file.name)
                ManagedPackageItem(
                    name = file.name,
                    path = file.absolutePath,
                    category = categoryFor(file.name).orEmpty(),
                    extension = extension,
                    size = file.length(),
                    modifiedAt = file.lastModified()
                )
            }
            .sortedByDescending { item -> item.modifiedAt }
            .toList()
    }

    override fun organizePackages(): PackageOrganizeResult {
        val settings = loadSettings()
        val targetRoot = File(settings.targetDirectory).also { root ->
            if (!root.exists()) {
                root.mkdirs()
            }
        }

        var movedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<String>()

        scanPackages().forEach { item ->
            val source = File(item.path)
            if (!source.exists()) {
                skippedCount += 1
                return@forEach
            }

            val categoryDir = File(targetRoot, item.category).also { directory ->
                if (!directory.exists()) {
                    directory.mkdirs()
                }
            }
            val target = uniqueTarget(categoryDir, source.name)

            val moved = runCatching {
                source.renameTo(target)
            }.getOrDefault(false)

            if (moved) {
                movedCount += 1
            } else {
                errors += "移动 ${source.absolutePath} 失败"
            }
        }

        return PackageOrganizeResult(
            movedCount = movedCount,
            skippedCount = skippedCount,
            errors = errors
        )
    }

    private fun uniqueTarget(directory: File, fileName: String): File {
        val baseName = fileName.substringBeforeLast('.', fileName)
        val suffix = fileName.substringAfterLast('.', "")
        var candidate = File(directory, fileName)
        var index = 1

        while (candidate.exists()) {
            val timestamp = Instant.now().epochSecond
            val name = if (suffix.isBlank()) {
                "$baseName-$timestamp-$index"
            } else {
                "$baseName-$timestamp-$index.$suffix"
            }
            candidate = File(directory, name)
            index += 1
        }

        return candidate
    }

    private fun extensionFor(fileName: String): String {
        val lowerName = fileName.lowercase()
        return when {
            lowerName.endsWith(".tar.gz") -> "tar.gz"
            lowerName.endsWith(".appimage") -> "appimage"
            else -> fileName.substringAfterLast('.', "").lowercase()
        }
    }

    private fun categoryFor(fileName: String): String? {
        val extension = extensionFor(fileName)
        return when (extension) {
            "dmg", "pkg", "ipa" -> "apple"
            "apk", "aab", "xapk" -> "android"
            "exe", "msi" -> "windows"
            "deb", "rpm", "appimage" -> "linux"
            "zip", "7z", "rar", "tar", "gz", "tar.gz" -> "archive"
            else -> null
        }
    }
}
