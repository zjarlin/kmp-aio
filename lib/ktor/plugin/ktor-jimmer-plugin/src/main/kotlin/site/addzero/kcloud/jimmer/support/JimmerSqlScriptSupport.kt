package site.addzero.kcloud.jimmer.support

import org.babyfish.jimmer.sql.dialect.DefaultDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import site.addzero.kcloud.jimmer.di.execute
import site.addzero.kcloud.jimmer.di.toRawKSqlClient
import javax.sql.DataSource

object JimmerSqlScriptSupport {
    fun loadClasspathSql(
        resourcePath: String,
        classLoader: ClassLoader = defaultClassLoader(),
    ): String {
        val normalizedPath = resourcePath.removePrefix("/")
        return classLoader.getResource(normalizedPath)?.readText().orEmpty()
    }

    fun executeStatements(
        sqlClient: KSqlClient,
        sql: String,
    ) {
        splitStatements(sql).forEach { statement ->
            sqlClient.execute(statement)
        }
    }

    fun executeClasspathSql(
        sqlClient: KSqlClient,
        resourcePath: String,
        classLoader: ClassLoader = defaultClassLoader(),
    ) {
        executeStatements(
            sqlClient = sqlClient,
            sql = loadClasspathSql(resourcePath = resourcePath, classLoader = classLoader),
        )
    }

    @Deprecated(
        message = "请改用 KSqlClient 版本，统一通过 Jimmer KSqlClient 承接原生 SQL。",
        replaceWith = ReplaceWith(
            "executeStatements(dataSource.toRawKSqlClient(DefaultDialect.INSTANCE), sql)",
            imports = [
                "org.babyfish.jimmer.sql.dialect.DefaultDialect",
                "site.addzero.kcloud.jimmer.di.toRawKSqlClient",
            ],
        ),
    )
    fun executeStatements(
        dataSource: DataSource,
        sql: String,
    ) {
        executeStatements(
            sqlClient = dataSource.toRawKSqlClient(DefaultDialect.INSTANCE),
            sql = sql,
        )
    }

    @Deprecated(
        message = "请改用 KSqlClient 版本，统一通过 Jimmer KSqlClient 承接原生 SQL。",
        replaceWith = ReplaceWith(
            "executeClasspathSql(dataSource.toRawKSqlClient(DefaultDialect.INSTANCE), resourcePath, classLoader)",
            imports = [
                "org.babyfish.jimmer.sql.dialect.DefaultDialect",
                "site.addzero.kcloud.jimmer.di.toRawKSqlClient",
            ],
        ),
    )
    fun executeClasspathSql(
        dataSource: DataSource,
        resourcePath: String,
        classLoader: ClassLoader = defaultClassLoader(),
    ) {
        executeClasspathSql(
            sqlClient = dataSource.toRawKSqlClient(DefaultDialect.INSTANCE),
            resourcePath = resourcePath,
            classLoader = classLoader,
        )
    }

    private fun splitStatements(
        sql: String,
    ): List<String> {
        return sql
            .lineSequence()
            .filterNot { line -> line.trimStart().startsWith("--") }
            .joinToString("\n")
            .split(";")
            .map(String::trim)
            .filter(String::isNotBlank)
    }

    private fun defaultClassLoader(): ClassLoader {
        return Thread.currentThread().contextClassLoader
            ?: JimmerSqlScriptSupport::class.java.classLoader
    }
}
