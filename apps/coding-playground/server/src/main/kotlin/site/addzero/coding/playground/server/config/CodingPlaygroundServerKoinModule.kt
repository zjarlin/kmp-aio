package site.addzero.coding.playground.server.config

import kotlinx.serialization.json.Json
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.sqlite.SQLiteDataSource
import java.nio.file.Files
import javax.sql.DataSource

@Module
@Configuration("coding-playground")
@ComponentScan("site.addzero.coding.playground.server")
class CodingPlaygroundServerKoinModule {
    @Single
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    }

    @Single
    fun provideSettings(): PlaygroundServerSettings {
        return PlaygroundServerSettings.fromSystem()
    }

    @Single(createdAtStart = true)
    fun provideDataSource(settings: PlaygroundServerSettings): DataSource {
        val dataSource = SQLiteDataSource().apply {
            url = settings.sqliteUrl
        }
        initDatabase(dataSource)
        return dataSource
    }

    @Single
    fun provideSqlClient(dataSource: DataSource): KSqlClient {
        return newKSqlClient {
            setDialect(SQLiteDialect())
            setDatabaseNamingStrategy(DefaultDatabaseNamingStrategy.LOWER_CASE)
            addScalarProvider(SqliteLocalDateTimeScalarProvider)
            setConnectionManager {
                val transactionalConnection = PlaygroundJdbcTransactionContext.connectionOrNull()
                if (transactionalConnection != null) {
                    proceed(transactionalConnection)
                } else {
                    dataSource.connection.use { proceed(it) }
                }
            }
        }
    }
}

fun initDatabase(dataSource: DataSource) {
    val sql = object {}.javaClass.getResource("/sql/schema-sqlite.sql")?.readText().orEmpty()
    if (sql.isBlank()) {
        return
    }
    dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            sql.split(";")
                .map(String::trim)
                .filter(String::isNotBlank)
                .forEach(statement::executeUpdate)
        }
    }
}
