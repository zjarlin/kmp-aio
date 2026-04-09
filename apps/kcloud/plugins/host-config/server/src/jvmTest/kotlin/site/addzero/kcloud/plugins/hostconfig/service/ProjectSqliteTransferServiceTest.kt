package site.addzero.kcloud.plugins.hostconfig.service

import java.io.File
import java.nio.file.Files
import java.sql.DriverManager
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import site.addzero.util.db.SqlExecutor

class ProjectSqliteTransferServiceTest {

    @Test
    fun shouldExportProjectToSqliteFile() {
        val tempRoot = Files.createTempDirectory("host-config-export").toFile()
        withProjectSqliteRoot(tempRoot) {
            ProjectServiceTestFixture().use { fixture ->
                val project = fixture.createProject("导出测试工程")
                val protocol = fixture.createProtocol(project.id, "导出协议")
                fixture.createModule(protocol.id, "导出模块")

                val service = ProjectSqliteTransferService(fixture.jdbc)
                val response = service.exportProjectSqlite(project.id)
                val exportedFile = File(response.filePath)

                assertTrue(exportedFile.exists(), "导出的 sqlite 文件必须存在")
                assertEquals(tempRoot.canonicalPath, exportedFile.parentFile.canonicalPath)
                assertTrue(response.summaryText.contains("工程 1 个"))

                val sqliteExecutor = SqlExecutor(sqliteDataSource(exportedFile))
                assertEquals(1L, sqliteExecutor.queryCount("SELECT COUNT(*) FROM host_config_project"))
                assertEquals(
                    "导出测试工程",
                    sqliteExecutor.query(
                        "SELECT name FROM host_config_project WHERE id = ?",
                        project.id,
                        mapper = { resultSet -> resultSet.getString(1) },
                    ).single(),
                )
            }
        }
    }

    @Test
    fun shouldImportSqliteIntoConfiguredDirectory() {
        val exportRoot = Files.createTempDirectory("host-config-export-source").toFile()
        val importRoot = Files.createTempDirectory("host-config-import-target").toFile()
        val exportedFile =
            withProjectSqliteRoot(exportRoot) {
                ProjectServiceTestFixture().use { fixture ->
                    val project = fixture.createProject("导入源工程")
                    val service = ProjectSqliteTransferService(fixture.jdbc)
                    File(service.exportProjectSqlite(project.id).filePath)
                }
            }

        withProjectSqliteRoot(importRoot) {
            ProjectServiceTestFixture().use { fixture ->
                val service = ProjectSqliteTransferService(fixture.jdbc)
                val response = service.importProjectSqlite(exportedFile.absolutePath)
                val importedFile = File(response.filePath)

                assertTrue(importedFile.exists(), "导入后的 sqlite 文件必须存在")
                assertEquals(importRoot.canonicalPath, importedFile.parentFile.canonicalPath)
                assertEquals(exportedFile.length(), importedFile.length())
                assertTrue(response.summaryText.contains("工程 1 个"))
            }
        }
    }
}

private inline fun <T> withProjectSqliteRoot(
    root: File,
    block: () -> T,
): T {
    val propertyName = "site.addzero.kcloud.hostConfig.projectSqliteRoot"
    val previousValue = System.getProperty(propertyName)
    System.setProperty(propertyName, root.absolutePath)
    try {
        return block()
    } finally {
        if (previousValue == null) {
            System.clearProperty(propertyName)
        } else {
            System.setProperty(propertyName, previousValue)
        }
    }
}

private fun sqliteDataSource(databaseFile: File): DataSource {
    Class.forName("org.sqlite.JDBC")
    val jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
    return object : DataSource {
        override fun getConnection() = DriverManager.getConnection(jdbcUrl)

        override fun getConnection(
            username: String?,
            password: String?,
        ) = getConnection()

        override fun getLogWriter() = null

        override fun setLogWriter(out: java.io.PrintWriter?) = Unit

        override fun setLoginTimeout(seconds: Int) = Unit

        override fun getLoginTimeout() = 0

        override fun getParentLogger() = java.util.logging.Logger.getLogger("org.sqlite")

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> unwrap(iface: Class<T>) = this as T

        override fun isWrapperFor(iface: Class<*>): Boolean = iface.isInstance(this)
    }
}
