package site.addzero.kcloud.platform

import kotlinx.browser.window

actual object DirectoryLauncher {
    actual fun openDirectory(path: String): Boolean {
        return runCatching {
            window.open(path, "_blank") != null
        }.getOrDefault(false)
    }
}
