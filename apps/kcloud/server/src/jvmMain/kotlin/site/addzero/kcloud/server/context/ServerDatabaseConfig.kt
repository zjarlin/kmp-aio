package site.addzero.kcloud.server.context

import java.io.File

internal const val KCLOUD_SERVER_MYSQL_SCHEMA: String = "okmy_dics"
internal const val KCLOUD_SERVER_MYSQL_USER: String = "root"
internal const val KCLOUD_SERVER_MYSQL_PASSWORD: String = "test123456"
internal const val KCLOUD_SERVER_DB_MODE_PROPERTY: String = "kcloud.server.db"
internal const val KCLOUD_SERVER_DB_MODE_ENV: String = "KCLOUD_SERVER_DB"
internal const val KCLOUD_SERVER_SQLITE_PATH_PROPERTY: String = "kcloud.server.sqlite.path"
internal const val KCLOUD_SERVER_SQLITE_PATH_ENV: String = "KCLOUD_SERVER_SQLITE_PATH"
internal const val KCLOUD_SERVER_DB_MODE_SQLITE: String = "sqlite"
internal const val KCLOUD_SERVER_DB_MODE_MYSQL: String = "mysql"

internal enum class KCloudServerDbMode {
    Sqlite,
    Mysql,
}

internal fun resolveKCloudServerDbMode(): KCloudServerDbMode {
    return when (readServerDatabaseModeValue().lowercase()) {
        KCLOUD_SERVER_DB_MODE_SQLITE -> KCloudServerDbMode.Sqlite
        KCLOUD_SERVER_DB_MODE_MYSQL -> KCloudServerDbMode.Mysql
        else -> KCloudServerDbMode.Mysql
    }
}

internal fun serverMysqlJdbcUrl(
    schema: String = KCLOUD_SERVER_MYSQL_SCHEMA,
): String {
    return "jdbc:mysql://192.168.31.133:3306/$schema?createDatabaseIfNotExist=true"
}

internal fun serverSqliteJdbcUrl(): String {
    val sqliteFile =
        File(
            readServerSqlitePathValue(),
        ).absoluteFile
    sqliteFile.parentFile?.mkdirs()
    return "jdbc:sqlite:${sqliteFile.absolutePath}"
}

private fun readServerDatabaseModeValue(): String {
    return System.getProperty(KCLOUD_SERVER_DB_MODE_PROPERTY)
        ?.takeIf(String::isNotBlank)
        ?: System.getenv(KCLOUD_SERVER_DB_MODE_ENV)
            ?.takeIf(String::isNotBlank)
        ?: KCLOUD_SERVER_DB_MODE_MYSQL
}

private fun readServerSqlitePathValue(): String {
    return System.getProperty(KCLOUD_SERVER_SQLITE_PATH_PROPERTY)
        ?.takeIf(String::isNotBlank)
        ?: System.getenv(KCLOUD_SERVER_SQLITE_PATH_ENV)
            ?.takeIf(String::isNotBlank)
        ?: File(
            File(System.getProperty("user.home"), ".kcloud"),
            "local/kcloud-server.sqlite",
        ).absolutePath
}
