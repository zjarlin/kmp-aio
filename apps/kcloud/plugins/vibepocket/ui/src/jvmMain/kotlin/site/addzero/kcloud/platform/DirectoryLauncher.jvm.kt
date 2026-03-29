package site.addzero.kcloud.platform

import java.awt.Desktop
import java.io.File

actual object DirectoryLauncher {
    actual fun openDirectory(path: String): Boolean {
        val directory = File(path).absoluteFile
        if (!directory.exists()) {
            return false
        }

        return runCatching {
            when {
                Desktop.isDesktopSupported() -> Desktop.getDesktop().open(directory)
                System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true) -> {
                    ProcessBuilder("open", directory.absolutePath).start()
                }

                System.getProperty("os.name").orEmpty().contains("Windows", ignoreCase = true) -> {
                    ProcessBuilder("explorer", directory.absolutePath).start()
                }

                else -> {
                    ProcessBuilder("xdg-open", directory.absolutePath).start()
                }
            }
            true
        }.getOrDefault(false)
    }
}
