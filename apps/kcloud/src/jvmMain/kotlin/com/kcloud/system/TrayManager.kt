package com.kcloud.system

import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

fun isSystemTraySupported(): Boolean {
    return SystemTray.isSupported()
}

class SystemTrayManager {
    private var trayIcon: TrayIcon? = null

    fun install(
        onShowWindow: () -> Unit,
        onExit: () -> Unit
    ): Boolean {
        if (!SystemTray.isSupported()) return false

        try {
            val tray = SystemTray.getSystemTray()

            // Create popup menu
            val popup = java.awt.PopupMenu()

            val showItem = java.awt.MenuItem("打开面板")
            showItem.addActionListener { onShowWindow() }
            popup.add(showItem)

            val mountItem = java.awt.MenuItem("挂载远程目录")
            mountItem.addActionListener { /* TODO */ }
            popup.add(mountItem)

            popup.addSeparator()

            val settingsItem = java.awt.MenuItem("设置")
            settingsItem.addActionListener { /* TODO */ }
            popup.add(settingsItem)

            popup.addSeparator()

            val exitItem = java.awt.MenuItem("退出")
            exitItem.addActionListener { onExit() }
            popup.add(exitItem)

            // Create tray icon
            val icon = loadTrayIcon()
            trayIcon = TrayIcon(icon, "MoveOff", popup).apply {
                isImageAutoSize = true
                addActionListener { onShowWindow() }
            }

            tray.add(trayIcon)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun uninstall() {
        trayIcon?.let {
            try {
                SystemTray.getSystemTray().remove(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        trayIcon = null
    }

    fun showNotification(
        title: String,
        message: String,
        type: TrayIcon.MessageType = TrayIcon.MessageType.INFO
    ) {
        trayIcon?.displayMessage(title, message, type)
    }

    private fun loadTrayIcon(): java.awt.Image {
        // Try to load icon from resources, fallback to default
        return try {
            val resource = Thread.currentThread().contextClassLoader.getResource("icon.png")
            if (resource != null) {
                Toolkit.getDefaultToolkit().getImage(resource)
            } else {
                createDefaultIcon()
            }
        } catch (e: Exception) {
            createDefaultIcon()
        }
    }

    private fun createDefaultIcon(): java.awt.Image {
        // Create a simple colored square as fallback icon
        val size = 16
        val image = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        g2d.color = java.awt.Color(0x3574F0)
        g2d.fillRect(0, 0, size, size)
        g2d.dispose()
        return image
    }
}
