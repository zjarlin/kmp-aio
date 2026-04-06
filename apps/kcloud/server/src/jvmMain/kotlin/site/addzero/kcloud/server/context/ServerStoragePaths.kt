package site.addzero.kcloud.server.context

import java.io.File

/**
 * 统一生成 SQLite JDBC 地址，避免数据库文件路径散落在各处。
 */
internal fun serverSqliteJdbcUrl(): String {
    val sqliteFile = File(ensureServerDataDirectory(), "kcloud.sqlite").absoluteFile
    return "jdbc:sqlite:${sqliteFile.absolutePath}"
}

/**
 * 统一把本地服务的数据目录固定到用户目录，避免跟工作目录耦合。
 */
private fun ensureServerDataDirectory(): File {
    val directory = File(System.getProperty("user.home"), ".kcloud/server").absoluteFile
    check(directory.mkdirs() || directory.isDirectory) {
        "无法创建 KCloud 服务数据目录：${directory.absolutePath}"
    }
    return directory
}
