package com.kcloud.paths

import java.io.File

object KCloudPaths {
    private const val workspaceSystemProperty = "kcloud.workspace.dir"
    private const val workspaceEnvVariable = "KCLOUD_WORKSPACE_DIR"

    fun appSupportDir(): File {
        return preferredAppSupportDir().ensureDirectory()
    }

    fun workspaceDir(): File {
        val overriddenPath = System.getProperty(workspaceSystemProperty)
            ?.takeIf { it.isNotBlank() }
            ?: System.getenv(workspaceEnvVariable)
                ?.takeIf { it.isNotBlank() }

        return (overriddenPath?.let(::File) ?: File(userHome(), "KCloud")).ensureDirectory()
    }

    fun pluginsDir(): File {
        return File(appSupportDir(), "plugins").ensureDirectory()
    }

    fun securityDir(): File {
        return File(appSupportDir(), "security").ensureDirectory()
    }

    private fun preferredAppSupportDir(): File {
        val userHome = userHome()
        val osName = System.getProperty("os.name").orEmpty()

        return when {
            osName.contains("Mac", ignoreCase = true) -> {
                File(userHome, "Library/Application Support/KCloud")
            }

            osName.contains("Windows", ignoreCase = true) -> {
                File(System.getenv("LOCALAPPDATA") ?: System.getenv("APPDATA") ?: userHome, "KCloud")
            }

            else -> {
                val xdgDataHome = System.getenv("XDG_DATA_HOME")
                    ?.takeIf { it.isNotBlank() }
                    ?: File(userHome, ".local/share").absolutePath
                File(xdgDataHome, "kcloud")
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
}
