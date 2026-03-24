package site.addzero.kcloud.features.desktop.system

import java.awt.SystemTray

fun isSystemTraySupported(): Boolean {
    return runCatching { SystemTray.isSupported() }.getOrDefault(false)
}
