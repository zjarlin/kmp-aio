package site.addzero.kcloud.plugins.hostconfig.service

import javax.sql.DataSource
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.support.JimmerSqlScriptSupport

/**
 * 宿主配置插件自己的表结构和种子数据初始化。
 *
 * 当前仓库里的 Jimmer 依赖没有把插件级 schema bootstrap SPI 暴露成源码入口，
 * 因此这里用插件自有的 eager singleton 在 Koin 启动阶段执行一次 SQL 资源，
 * 保证表结构和协议字典由插件自己负责。
 */
@Single(createdAtStart = true)
class HostConfigSchemaBootstrap(
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
                    "sql/host-config-schema-sqlite.sql"
                }

                productName.contains("mysql", ignoreCase = true) ||
                    jdbcUrl.contains(":mysql:", ignoreCase = true) -> {
                    "sql/host-config-schema-mysql.sql"
                }

                else -> {
                    error(
                        "HostConfigSchemaBootstrap does not support database '$productName' ($jdbcUrl). " +
                            "Please provide a matching schema script.",
                    )
                }
            }
        }
    }
}
