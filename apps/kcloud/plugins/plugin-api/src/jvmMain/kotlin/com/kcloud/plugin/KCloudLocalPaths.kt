package com.kcloud.plugin

import java.io.File

object KCloudLocalPaths {
    fun appSupportDir(): File {
        val userHome = System.getProperty("user.home")
        return when {
            System.getProperty("os.name").contains("Mac", ignoreCase = true) ->
                File(userHome, "Library/Application Support/MoveOff")

            System.getProperty("os.name").contains("Windows", ignoreCase = true) ->
                File(System.getenv("APPDATA") ?: userHome, "MoveOff")

            else ->
                File(userHome, ".config/moveoff")
        }.also { directory ->
            if (!directory.exists()) {
                directory.mkdirs()
            }
        }
    }

    fun pluginDir(pluginId: String): File {
        return File(appSupportDir(), "plugins/$pluginId").also { directory ->
            if (!directory.exists()) {
                directory.mkdirs()
            }
        }
    }
}
