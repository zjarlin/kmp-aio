package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import javax.sql.DataSource
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.support.JimmerSqlScriptSupport

@Single(createdAtStart = true)
class CodegenContextSchemaBootstrap(
    dataSource: DataSource,
) {
    init {
        JimmerSqlScriptSupport.executeClasspathSql(
            dataSource = dataSource,
            resourcePath = resolveSchemaScript(dataSource),
        )
    }

    private fun resolveSchemaScript(
        dataSource: DataSource,
    ): String {
        dataSource.connection.use { connection ->
            val productName = connection.metaData.databaseProductName
            val jdbcUrl = connection.metaData.url

            return when {
                productName.contains("sqlite", ignoreCase = true) ||
                    jdbcUrl.contains(":sqlite:", ignoreCase = true) -> {
                    "sql/codegen-context-schema-sqlite.sql"
                }

                productName.contains("mysql", ignoreCase = true) ||
                    jdbcUrl.contains(":mysql:", ignoreCase = true) -> {
                    "sql/codegen-context-schema-mysql.sql"
                }

                else -> {
                    error(
                        "CodegenContextSchemaBootstrap does not support database '$productName' ($jdbcUrl). " +
                            "Please provide a matching schema script.",
                    )
                }
            }
        }
    }
}
