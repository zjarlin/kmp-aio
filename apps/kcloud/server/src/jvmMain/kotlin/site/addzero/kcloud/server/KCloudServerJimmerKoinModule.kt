package site.addzero.kcloud.server

import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.spi.DatasourceBootstrapContext
import site.addzero.kcloud.jimmer.spi.JimmerDatasourceBootstrapSpi
import site.addzero.kcloud.jimmer.support.JimmerSqlScriptSupport

private const val SQLITE_SCHEMA_RESOURCE = "sql/schema-sqlite.sql"
private const val POSTGRES_SCHEMA_RESOURCE = "sql/schema-postgres.sql"

@Module
class KCloudServerJimmerKoinModule {
    @Single
    fun provideDatasourceBootstrapper(): JimmerDatasourceBootstrapSpi {
        return KCloudServerSchemaBootstrapper()
    }
}

private class KCloudServerSchemaBootstrapper : JimmerDatasourceBootstrapSpi {
    override fun onDataSourceReady(
        context: DatasourceBootstrapContext,
    ) {
        when {
            context.properties.driver.contains("sqlite", ignoreCase = true) -> {
                JimmerSqlScriptSupport.executeClasspathSql(
                    dataSource = context.dataSource,
                    resourcePath = SQLITE_SCHEMA_RESOURCE,
                )
                normalizeSqliteEpochDateTimeColumns(context)
            }

            context.properties.driver.contains("postgres", ignoreCase = true) -> {
                JimmerSqlScriptSupport.executeClasspathSql(
                    dataSource = context.dataSource,
                    resourcePath = POSTGRES_SCHEMA_RESOURCE,
                )
            }
        }
    }

    private fun normalizeSqliteEpochDateTimeColumns(
        context: DatasourceBootstrapContext,
    ) {
        if (context.driver.dialect() !is SQLiteDialect) {
            return
        }
        val tableColumns = mapOf(
            "music_task" to listOf("created_at", "updated_at"),
            "favorite_track" to listOf("created_at"),
            "music_history" to listOf("created_at"),
            "persona_record" to listOf("created_at"),
            "suno_task_resource" to listOf("created_at", "updated_at"),
            "user_profile" to listOf("create_time", "update_time"),
            "rbac_role" to listOf("create_time", "update_time"),
            "ai_chat_session" to listOf("create_time", "update_time"),
            "ai_chat_message" to listOf("create_time", "update_time"),
            "knowledge_space" to listOf("create_time", "update_time"),
            "knowledge_document" to listOf("create_time", "update_time"),
            "config_center_project" to listOf("create_time", "update_time"),
            "config_center_environment" to listOf("create_time", "update_time"),
            "config_center_config" to listOf("create_time", "update_time"),
            "config_center_secret" to listOf("create_time", "update_time"),
            "config_center_secret_version" to listOf("create_time", "update_time"),
            "config_center_service_token" to listOf("create_time", "update_time"),
            "config_center_activity_log" to listOf("create_time", "update_time"),
        )
        context.dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                tableColumns.forEach { (table, columns) ->
                    columns.forEach { column ->
                        statement.executeUpdate(
                            """
                            UPDATE $table
                            SET $column = datetime(CAST($column AS INTEGER) / 1000, 'unixepoch', 'localtime')
                            WHERE trim($column) GLOB '[0-9]*'
                              AND length(trim($column)) >= 10
                              AND instr($column, '-') = 0
                              AND instr($column, ':') = 0
                            """.trimIndent()
                        )
                    }
                }
            }
        }
    }
}
