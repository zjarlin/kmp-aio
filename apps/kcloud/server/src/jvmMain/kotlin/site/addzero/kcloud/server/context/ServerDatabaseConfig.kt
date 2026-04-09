package site.addzero.kcloud.server.context

internal const val KCLOUD_SERVER_MYSQL_SCHEMA: String = "okmy_dics"
internal const val KCLOUD_SERVER_MYSQL_USER: String = "root"
internal const val KCLOUD_SERVER_MYSQL_PASSWORD: String = "test123456"

internal fun serverMysqlJdbcUrl(
    schema: String = KCLOUD_SERVER_MYSQL_SCHEMA,
): String {
    return "jdbc:mysql://192.168.31.133:3306/$schema?createDatabaseIfNotExist=true"
}
