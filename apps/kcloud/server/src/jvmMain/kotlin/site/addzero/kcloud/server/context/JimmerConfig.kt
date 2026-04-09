package site.addzero.kcloud.server.context

import java.io.File
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.spi.DatasourceProperties
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi

const val KCLOUD_SERVER_DB_MODE_SQLITE: String = "sqlite"

private val KCLOUD_SERVER_LOCAL_DIRECTORY =
    File(
        System.getProperty("user.home"),
        ".kcloud/local",
    )

private val KCLOUD_SERVER_SQLITE_FILE: File =
    File(
        KCLOUD_SERVER_LOCAL_DIRECTORY,
        "kcloud-server.sqlite",
    )

fun kcloudServerLocalDataDirectory(): File {
    KCLOUD_SERVER_LOCAL_DIRECTORY.mkdirs()
    return KCLOUD_SERVER_LOCAL_DIRECTORY
}

fun kcloudServerSqliteFile(): File {
    val directory = kcloudServerLocalDataDirectory()
    if (!KCLOUD_SERVER_SQLITE_FILE.parentFile.exists()) {
        directory.mkdirs()
    }
    return KCLOUD_SERVER_SQLITE_FILE
}

fun serverSqliteJdbcUrl(): String {
    return "jdbc:sqlite:${kcloudServerSqliteFile().absolutePath}"
}

@Single
class JimmerConfig : DatasourcePropertiesSpi {
    override fun datasources(): List<DatasourceProperties> {
        return listOf(
            DatasourceProperties(
                name = KCLOUD_SERVER_DB_MODE_SQLITE,
                enabled = true,
                default = true,
                url = serverSqliteJdbcUrl(),
                driverClassName = "org.sqlite.JDBC",
            ),
        )
    }
}
