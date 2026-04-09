package site.addzero.kcloud.server.context

import java.io.File
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.spi.DatasourceProperties
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi
import site.addzero.kcloud.server.context.KCLOUD_SERVER_LOCAL_DIRECTORY
import site.addzero.kcloud.server.context.KCLOUD_SERVER_SQLITE_FILE
import site.addzero.kcloud.server.context.kcloudServerLocalDataDirectory
import kotlin.collections.listOf

private val KCLOUD_SERVER_LOCAL_DIRECTORY = File(System.getProperty("user.home"), ".kcloud/local")

private val KCLOUD_SERVER_SQLITE_FILE = File(KCLOUD_SERVER_LOCAL_DIRECTORY, "kcloud-server.sqlite")

fun kcloudServerLocalDataDirectory(): File {
    KCLOUD_SERVER_LOCAL_DIRECTORY.mkdirs()
    return KCLOUD_SERVER_LOCAL_DIRECTORY
}

@Single
class JimmerConfig : DatasourcePropertiesSpi {
    override fun datasources(): List<DatasourceProperties> {
        val directory = kcloudServerLocalDataDirectory()
        if (!KCLOUD_SERVER_SQLITE_FILE.parentFile.exists()) {
            directory.mkdirs()
        }
        return listOf(
            DatasourceProperties(
                name = "sqlite",
                enabled = true,
                default = true,
                url = "jdbc:sqlite:${KCLOUD_SERVER_SQLITE_FILE.absolutePath}",
                driverClassName = "org.sqlite.JDBC",
            ),
        )
    }
}
