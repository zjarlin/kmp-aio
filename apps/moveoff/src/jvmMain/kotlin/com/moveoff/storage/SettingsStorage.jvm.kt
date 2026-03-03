package com.moveoff.storage

import java.io.File

actual fun getSettingsDirectory(): File {
    val userHome = System.getProperty("user.home")
    return when {
        System.getProperty("os.name").contains("Mac", ignoreCase = true) ->
            File(userHome, "Library/Application Support/MoveOff")
        System.getProperty("os.name").contains("Windows", ignoreCase = true) ->
            File(System.getenv("APPDATA") ?: userHome, "MoveOff")
        else ->
            File(userHome, ".config/moveoff")
    }
}
