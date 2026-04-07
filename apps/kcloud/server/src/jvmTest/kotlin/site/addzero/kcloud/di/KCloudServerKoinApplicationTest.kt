package site.addzero.kcloud.di

import io.ktor.server.application.Application
import javax.sql.DataSource
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.dsl.koinApplication
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.kcloud.jimmer.spi.DatasourceProperties
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi
import site.addzero.kcloud.plugins.hostconfig.service.ProjectService
import site.addzero.starter.AppStarter
import site.addzero.starter.banner.BannerStarter
import site.addzero.starter.openapi.OpenApiStarter
import site.addzero.starter.serialization.SerializationStarter
import site.addzero.starter.statuspages.StatusPagesStarter

@Module
class ServerTestDatasourceOverrideModule {
    @Single
    fun datasourcePropertiesSpi(): DatasourcePropertiesSpi {
        return SqliteTestDatasourcePropertiesSpi(name = "server")
    }
}

class KCloudServerKoinApplicationTest {
    @Test
    fun serverRootLoadsBroadScanStartersAndExplicitPlainModules() {
        val app = koinApplication {
            allowOverride(true)
            withConfiguration<KCloudServerKoinApplication>()
            modules(ServerTestDatasourceOverrideModule().module())
        }

        try {
            val starters = app.koin.getAll<AppStarter<Application>>()

            assertTrue(starters.any { it is BannerStarter })
            assertTrue(starters.any { it is OpenApiStarter })
            assertTrue(starters.any { it is SerializationStarter })
            assertTrue(starters.any { it is StatusPagesStarter })
            assertNotNull(app.koin.get<DataSource>())
            assertNotNull(app.koin.get<ProjectService>())
        } finally {
            app.close()
        }
    }
}

private class SqliteTestDatasourcePropertiesSpi(
    name: String,
) : DatasourcePropertiesSpi {
    private val jdbcUrl = buildTestSqliteJdbcUrl(name)

    override fun datasources(): List<DatasourceProperties> {
        return listOf(
            DatasourceProperties(
                name = "sqlite",
                enabled = true,
                default = true,
                url = jdbcUrl,
                driverClassName = "org.sqlite.JDBC",
            ),
        )
    }
}

private fun buildTestSqliteJdbcUrl(
    name: String,
): String {
    val file = File(System.getProperty("java.io.tmpdir"), "kcloud-koin-$name.db")
    file.delete()
    return "jdbc:sqlite:${file.absolutePath}"
}
