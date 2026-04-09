package site.addzero.kcloud.shell.spi_impl

import java.awt.Desktop
import java.io.File
import site.addzero.kcloud.server.context.kcloudServerLocalDataDirectory

/**
 * 打开本地 SQLite 数据目录。
 */
fun openKCloudLocalDataDirectory() {
    val directory = kcloudServerLocalDataDirectory()
    require(Desktop.isDesktopSupported()) {
        "当前桌面环境不支持打开目录：${directory.absolutePath}"
    }
    Desktop.getDesktop().open(directory)
}

/**
 * 返回本地 SQLite 数据目录。
 */
fun kcloudLocalDataDirectory(): File {
    return kcloudServerLocalDataDirectory()
}
