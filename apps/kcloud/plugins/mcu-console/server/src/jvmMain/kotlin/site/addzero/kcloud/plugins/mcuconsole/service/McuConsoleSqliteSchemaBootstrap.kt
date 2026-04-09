package site.addzero.kcloud.plugins.mcuconsole.service

import io.ktor.server.application.Application
import io.ktor.server.application.log
import java.sql.Connection
import javax.sql.DataSource
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter

private const val MCU_CONSOLE_SQLITE_SCHEMA_RESOURCE = "/db/sqlite/mcu-console-local-schema.sql"

@Named("mcuConsoleSqliteSchemaBootstrap")
@Single
class McuConsoleSqliteSchemaBootstrap(
    private val dataSource: DataSource,
) : AppStarter {
    override val order: Int = 130
    override val enable: Boolean = true

    override fun onInstall(application: Application) {
        if (!ensureMcuConsoleSqliteSchema(dataSource)) {
            return
        }
        application.log.info("Mcu-console SQLite schema is ready.")
    }
}

internal fun ensureMcuConsoleSqliteSchema(
    dataSource: DataSource,
): Boolean {
    dataSource.connection.use { connection ->
        if (!connection.isSqliteConnection()) {
            return false
        }
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON")
        }
        executeSqliteSchemaScript(
            owner = McuConsoleSqliteSchemaBootstrap::class.java,
            connection = connection,
            resourcePath = MCU_CONSOLE_SQLITE_SCHEMA_RESOURCE,
        )
    }
    return true
}

private fun executeSqliteSchemaScript(
    owner: Class<*>,
    connection: Connection,
    resourcePath: String,
) {
    val script =
        checkNotNull(owner.getResource(resourcePath)) {
            "Missing SQLite schema resource: $resourcePath"
        }.readText()
    connection.createStatement().use { statement ->
        script.splitToSequence(';')
            .map(String::trim)
            .filter(String::isNotBlank)
            .forEach(statement::execute)
    }
}

private fun Connection.isSqliteConnection(): Boolean = metaData.url.startsWith("jdbc:sqlite:")
