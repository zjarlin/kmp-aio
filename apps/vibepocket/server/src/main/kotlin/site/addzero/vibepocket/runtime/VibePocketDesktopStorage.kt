package site.addzero.vibepocket.runtime

import java.io.File

data class VibePocketDesktopStoragePaths(
    val dataDir: File,
    val cacheDir: File,
    val sqliteFile: File,
) {
    val sqliteJdbcUrl: String
        get() = "jdbc:sqlite:${sqliteFile.absolutePath}"
}

object VibePocketDesktopStorage {
    private const val appDirectoryName = "VibePocket"

    fun resolve(): VibePocketDesktopStoragePaths {
        val dataDir = preferredDataDir().ensureDirectory()
        val cacheDir = preferredCacheDir().ensureDirectory()
        val sqliteFile = File(dataDir, "vibepocket.db").absoluteFile
        migrateLegacyDatabaseIfNeeded(sqliteFile)
        return VibePocketDesktopStoragePaths(
            dataDir = dataDir,
            cacheDir = cacheDir,
            sqliteFile = sqliteFile,
        )
    }

    private fun preferredDataDir(): File {
        val userHome = userHome()
        val osName = System.getProperty("os.name").orEmpty()

        return when {
            osName.contains("Mac", ignoreCase = true) -> {
                File(userHome, "Library/Application Support/$appDirectoryName")
            }

            osName.contains("Windows", ignoreCase = true) -> {
                File(
                    System.getenv("LOCALAPPDATA")
                        ?: System.getenv("APPDATA")
                        ?: userHome,
                    appDirectoryName,
                )
            }

            else -> {
                val xdgDataHome = System.getenv("XDG_DATA_HOME")
                    ?.takeIf { it.isNotBlank() }
                    ?: File(userHome, ".local/share").absolutePath
                File(xdgDataHome, appDirectoryName.lowercase())
            }
        }
    }

    private fun preferredCacheDir(): File {
        val userHome = userHome()
        val osName = System.getProperty("os.name").orEmpty()

        return when {
            osName.contains("Mac", ignoreCase = true) -> {
                File(userHome, "Library/Caches/$appDirectoryName")
            }

            osName.contains("Windows", ignoreCase = true) -> {
                File(
                    System.getenv("LOCALAPPDATA")
                        ?: System.getenv("APPDATA")
                        ?: userHome,
                    "$appDirectoryName/Cache",
                )
            }

            else -> {
                val xdgCacheHome = System.getenv("XDG_CACHE_HOME")
                    ?.takeIf { it.isNotBlank() }
                    ?: File(userHome, ".cache").absolutePath
                File(xdgCacheHome, appDirectoryName.lowercase())
            }
        }
    }

    private fun File.ensureDirectory(): File {
        if (exists()) {
            require(isDirectory) {
                "路径不是目录：$absolutePath"
            }
            return this
        }

        check(mkdirs() || exists()) {
            "无法创建目录：$absolutePath"
        }
        return this
    }

    private fun userHome(): String {
        return System.getProperty("user.home").orEmpty()
    }

    private fun migrateLegacyDatabaseIfNeeded(targetFile: File) {
        if (targetFile.exists()) {
            return
        }

        val legacySource = legacyDatabaseCandidates()
            .firstOrNull { candidate ->
                candidate.exists() &&
                    candidate.isFile &&
                    candidate.length() > 0L &&
                    candidate.absolutePath != targetFile.absolutePath
            }
            ?: return

        runCatching {
            legacySource.copyTo(targetFile, overwrite = false)
        }.onFailure {
            println("Failed to migrate legacy vibepocket database from ${legacySource.absolutePath}: ${it.message}")
        }
    }

    private fun legacyDatabaseCandidates(): List<File> {
        val workingDir = File(System.getProperty("user.dir").orEmpty()).absoluteFile
        val parentDir = workingDir.parentFile

        return buildList {
            add(File(workingDir, "vibepocket-dev.db"))
            add(File(workingDir, "vibepocket.db"))
            add(File(workingDir, "vibepocket-desktop.db"))
            add(File(workingDir, "server/vibepocket.db"))
            if (parentDir != null) {
                add(File(parentDir, "vibepocket-dev.db"))
                add(File(parentDir, "vibepocket.db"))
                add(File(parentDir, "vibepocket-desktop.db"))
            }
        }.distinctBy { it.absolutePath }
    }
}
