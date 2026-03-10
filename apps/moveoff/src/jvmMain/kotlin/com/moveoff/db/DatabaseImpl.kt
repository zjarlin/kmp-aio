package com.moveoff.db

import com.moveoff.model.ConflictStrategy
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp

/**
 * JDBC数据库实现
 */
class DatabaseImpl(
    private val dbPath: String = getDefaultDbPath()
) : Database {

    private lateinit var dataSource: HikariDataSource
    private val _fileRecordsFlow = MutableStateFlow<List<FileRecord>>(emptyList())
    private val _queueFlow = MutableStateFlow<List<SyncQueueItem>>(emptyList())

    companion object {
        private const val CURRENT_VERSION = 1

        fun getDefaultDbPath(): String {
            val userHome = System.getProperty("user.home")
            val appDir = File(userHome, ".moveoff")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            return File(appDir, "sync.db").absolutePath
        }
    }

    override fun initialize() {
        try {
            // 配置HikariCP连接池
            val config = HikariConfig().apply {
                jdbcUrl = "jdbc:sqlite:$dbPath"
                driverClassName = "org.sqlite.JDBC"
                maximumPoolSize = 1  // SQLite只支持单连接写入
                connectionTestQuery = "SELECT 1"
            }

            dataSource = HikariDataSource(config)

            // 初始化表结构
            createTables()

            // 加载初始数据到Flow
            refreshFlows()

        } catch (e: Exception) {
            throw DatabaseException("数据库初始化失败: ${e.message}", e)
        }
    }

    override fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }

    /**
     * 创建表结构
     */
    private fun createTables() {
        dataSource.connection.use { conn ->
            // 文件记录表
            conn.createStatement().execute(
                """
                CREATE TABLE IF NOT EXISTS files (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    path TEXT UNIQUE NOT NULL,
                    local_mtime INTEGER,
                    local_size INTEGER,
                    local_hash TEXT,
                    remote_etag TEXT,
                    remote_version_id TEXT,
                    remote_mtime INTEGER,
                    remote_size INTEGER,
                    sync_state TEXT NOT NULL DEFAULT 'SYNCED',
                    last_sync_time INTEGER,
                    conflict_strategy TEXT,
                    created_at INTEGER DEFAULT (strftime('%s', 'now') * 1000),
                    updated_at INTEGER DEFAULT (strftime('%s', 'now') * 1000)
                )
                """.trimIndent()
            )

            // 创建索引
            conn.createStatement().execute(
                "CREATE INDEX IF NOT EXISTS idx_files_path ON files(path)"
            )
            conn.createStatement().execute(
                "CREATE INDEX IF NOT EXISTS idx_files_state ON files(sync_state)"
            )

            // 同步队列表
            conn.createStatement().execute(
                """
                CREATE TABLE IF NOT EXISTS sync_queue (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    file_id INTEGER NOT NULL REFERENCES files(id) ON DELETE CASCADE,
                    operation TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    progress_bytes INTEGER DEFAULT 0,
                    total_bytes INTEGER NOT NULL,
                    retry_count INTEGER DEFAULT 0,
                    error_message TEXT,
                    created_at INTEGER DEFAULT (strftime('%s', 'now') * 1000),
                    updated_at INTEGER DEFAULT (strftime('%s', 'now') * 1000)
                )
                """.trimIndent()
            )

            // 队列索引
            conn.createStatement().execute(
                "CREATE INDEX IF NOT EXISTS idx_queue_status ON sync_queue(status)"
            )
            conn.createStatement().execute(
                "CREATE INDEX IF NOT EXISTS idx_queue_file ON sync_queue(file_id)"
            )

            // 数据库版本记录
            conn.createStatement().execute(
                """
                CREATE TABLE IF NOT EXISTS db_version (
                    version INTEGER PRIMARY KEY
                )
                """.trimIndent()
            )

            // 插入或更新版本
            conn.prepareStatement(
                "INSERT OR REPLACE INTO db_version (version) VALUES (?)"
            ).use { stmt ->
                stmt.setInt(1, CURRENT_VERSION)
                stmt.executeUpdate()
            }
        }
    }

    override fun getOrCreateFileRecord(path: String): FileRecord {
        return getFileRecord(path) ?: run {
            dataSource.connection.use { conn ->
                conn.prepareStatement(
                    """
                    INSERT INTO files (path, sync_state, updated_at)
                    VALUES (?, 'SYNCED', ?)
                    ON CONFLICT(path) DO UPDATE SET updated_at = excluded.updated_at
                    """.trimIndent()
                ).use { stmt ->
                    stmt.setString(1, path)
                    stmt.setLong(2, System.currentTimeMillis())
                    stmt.executeUpdate()
                }
            }
            refreshFlows()
            getFileRecord(path)!!
        }
    }

    override fun getFileRecord(path: String): FileRecord? {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT * FROM files WHERE path = ?"
            ).use { stmt ->
                stmt.setString(1, path)
                stmt.executeQuery().use { rs ->
                    return if (rs.next()) rs.toFileRecord() else null
                }
            }
        }
    }

    override fun getAllFileRecords(): List<FileRecord> {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery("SELECT * FROM files ORDER BY path").use { rs ->
                    return generateSequence {
                        if (rs.next()) rs.toFileRecord() else null
                    }.toList()
                }
            }
        }
    }

    override fun getFileRecordsByState(state: SyncState): List<FileRecord> {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT * FROM files WHERE sync_state = ? ORDER BY path"
            ).use { stmt ->
                stmt.setString(1, state.name)
                stmt.executeQuery().use { rs ->
                    return generateSequence {
                        if (rs.next()) rs.toFileRecord() else null
                    }.toList()
                }
            }
        }
    }

    override fun updateLocalInfo(path: String, mtime: Long, size: Long, hash: String?): Boolean {
        val updated = dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                UPDATE files
                SET local_mtime = ?, local_size = ?, local_hash = ?, updated_at = ?
                WHERE path = ?
                """.trimIndent()
            ).use { stmt ->
                stmt.setLong(1, mtime)
                stmt.setLong(2, size)
                stmt.setString(3, hash)
                stmt.setLong(4, System.currentTimeMillis())
                stmt.setString(5, path)
                stmt.executeUpdate()
            }
        }
        if (updated > 0) refreshFlows()
        return updated > 0
    }

    override fun updateRemoteInfo(
        path: String,
        etag: String,
        versionId: String?,
        mtime: Long?,
        size: Long?
    ): Boolean {
        val updated = dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                UPDATE files
                SET remote_etag = ?, remote_version_id = ?, remote_mtime = ?, remote_size = ?, updated_at = ?
                WHERE path = ?
                """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, etag)
                stmt.setString(2, versionId)
                mtime?.let { stmt.setLong(3, it) } ?: stmt.setNull(3, java.sql.Types.BIGINT)
                size?.let { stmt.setLong(4, it) } ?: stmt.setNull(4, java.sql.Types.BIGINT)
                stmt.setLong(5, System.currentTimeMillis())
                stmt.setString(6, path)
                stmt.executeUpdate()
            }
        }
        if (updated > 0) refreshFlows()
        return updated > 0
    }

    override fun updateSyncState(path: String, state: SyncState, lastSyncTime: Long?): Boolean {
        val updated = dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                UPDATE files
                SET sync_state = ?, last_sync_time = ?, updated_at = ?
                WHERE path = ?
                """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, state.name)
                lastSyncTime?.let { stmt.setLong(2, it) } ?: stmt.setNull(2, java.sql.Types.BIGINT)
                stmt.setLong(3, System.currentTimeMillis())
                stmt.setString(4, path)
                stmt.executeUpdate()
            }
        }
        if (updated > 0) refreshFlows()
        return updated > 0
    }

    override fun setConflictStrategy(path: String, strategy: ConflictStrategy): Boolean {
        val updated = dataSource.connection.use { conn ->
            conn.prepareStatement(
                "UPDATE files SET conflict_strategy = ?, updated_at = ? WHERE path = ?"
            ).use { stmt ->
                stmt.setString(1, strategy.name)
                stmt.setLong(2, System.currentTimeMillis())
                stmt.setString(3, path)
                stmt.executeUpdate()
            }
        }
        return updated > 0
    }

    override fun deleteFileRecord(path: String): Boolean {
        val deleted = dataSource.connection.use { conn ->
            conn.prepareStatement("DELETE FROM files WHERE path = ?").use { stmt ->
                stmt.setString(1, path)
                stmt.executeUpdate()
            }
        }
        if (deleted > 0) refreshFlows()
        return deleted > 0
    }

    override fun resolveConflict(path: String, resolution: ConflictResolution): Boolean {
        return dataSource.connection.use { conn ->
            conn.autoCommit = false
            try {
                // 获取文件ID
                val fileId = conn.prepareStatement(
                    "SELECT id FROM files WHERE path = ?"
                ).use { stmt ->
                    stmt.setString(1, path)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) rs.getLong("id") else null
                    }
                } ?: return false

                // 删除相关队列项
                conn.prepareStatement(
                    "DELETE FROM sync_queue WHERE file_id = ?"
                ).use { stmt ->
                    stmt.setLong(1, fileId)
                    stmt.executeUpdate()
                }

                // 更新文件状态
                val newState = when (resolution) {
                    ConflictResolution.USE_LOCAL -> SyncState.PENDING_UPLOAD
                    ConflictResolution.USE_REMOTE -> SyncState.PENDING_DOWNLOAD
                    ConflictResolution.KEEP_BOTH -> SyncState.PENDING_UPLOAD
                    ConflictResolution.MERGE -> SyncState.PENDING_UPLOAD
                }

                conn.prepareStatement(
                    "UPDATE files SET sync_state = ?, conflict_strategy = NULL, updated_at = ? WHERE id = ?"
                ).use { stmt ->
                    stmt.setString(1, newState.name)
                    stmt.setLong(2, System.currentTimeMillis())
                    stmt.setLong(3, fileId)
                    stmt.executeUpdate()
                }

                conn.commit()
                refreshFlows()
                true
            } catch (e: Exception) {
                conn.rollback()
                throw DatabaseException("解决冲突失败: ${e.message}", e)
            } finally {
                conn.autoCommit = true
            }
        }
    }

    override fun enqueueSync(path: String, operation: SyncOperation, totalBytes: Long): Long {
        val fileRecord = getOrCreateFileRecord(path)

        return dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                INSERT INTO sync_queue (file_id, operation, status, total_bytes, created_at, updated_at)
                VALUES (?, ?, 'PENDING', ?, ?, ?)
                """.trimIndent(),
                java.sql.Statement.RETURN_GENERATED_KEYS
            ).use { stmt ->
                val now = System.currentTimeMillis()
                stmt.setLong(1, fileRecord.id)
                stmt.setString(2, operation.name)
                stmt.setLong(3, totalBytes)
                stmt.setLong(4, now)
                stmt.setLong(5, now)
                stmt.executeUpdate()

                stmt.generatedKeys.use { rs ->
                    if (rs.next()) {
                        val id = rs.getLong(1)
                        refreshFlows()
                        id
                    } else {
                        throw DatabaseException("无法获取插入的队列ID")
                    }
                }
            }
        }
    }

    override fun getQueueItems(): List<SyncQueueItem> {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(
                    """
                    SELECT q.*, f.path as file_path
                    FROM sync_queue q
                    JOIN files f ON q.file_id = f.id
                    ORDER BY q.created_at
                    """.trimIndent()
                ).use { rs ->
                    return generateSequence {
                        if (rs.next()) rs.toSyncQueueItem() else null
                    }.toList()
                }
            }
        }
    }

    override fun getPendingQueueItems(limit: Int): List<SyncQueueItem> {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                SELECT q.*, f.path as file_path
                FROM sync_queue q
                JOIN files f ON q.file_id = f.id
                WHERE q.status = 'PENDING'
                ORDER BY q.created_at
                LIMIT ?
                """.trimIndent()
            ).use { stmt ->
                stmt.setInt(1, limit)
                stmt.executeQuery().use { rs ->
                    return generateSequence {
                        if (rs.next()) rs.toSyncQueueItem() else null
                    }.toList()
                }
            }
        }
    }

    override fun getRunningQueueItems(): List<SyncQueueItem> {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(
                    """
                    SELECT q.*, f.path as file_path
                    FROM sync_queue q
                    JOIN files f ON q.file_id = f.id
                    WHERE q.status = 'RUNNING'
                    ORDER BY q.updated_at DESC
                    """.trimIndent()
                ).use { rs ->
                    return generateSequence {
                        if (rs.next()) rs.toSyncQueueItem() else null
                    }.toList()
                }
            }
        }
    }

    override fun updateQueueStatus(
        queueId: Long,
        status: QueueStatus,
        progressBytes: Long?,
        errorMessage: String?
    ): Boolean {
        val updated = dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                UPDATE sync_queue
                SET status = ?, progress_bytes = COALESCE(?, progress_bytes), error_message = ?, updated_at = ?
                WHERE id = ?
                """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, status.name)
                progressBytes?.let { stmt.setLong(2, it) } ?: stmt.setNull(2, java.sql.Types.BIGINT)
                stmt.setString(3, errorMessage)
                stmt.setLong(4, System.currentTimeMillis())
                stmt.setLong(5, queueId)
                stmt.executeUpdate()
            }
        }
        if (updated > 0) refreshFlows()
        return updated > 0
    }

    override fun incrementRetryCount(queueId: Long): Boolean {
        val updated = dataSource.connection.use { conn ->
            conn.prepareStatement(
                "UPDATE sync_queue SET retry_count = retry_count + 1, updated_at = ? WHERE id = ?"
            ).use { stmt ->
                stmt.setLong(1, System.currentTimeMillis())
                stmt.setLong(2, queueId)
                stmt.executeUpdate()
            }
        }
        if (updated > 0) refreshFlows()
        return updated > 0
    }

    override fun completeQueueItem(queueId: Long): Boolean {
        return dataSource.connection.use { conn ->
            conn.autoCommit = false
            try {
                // 获取文件路径
                val fileId = conn.prepareStatement(
                    "SELECT file_id FROM sync_queue WHERE id = ?"
                ).use { stmt ->
                    stmt.setLong(1, queueId)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) rs.getLong("file_id") else null
                    }
                }

                // 删除队列项
                conn.prepareStatement(
                    "DELETE FROM sync_queue WHERE id = ?"
                ).use { stmt ->
                    stmt.setLong(1, queueId)
                    stmt.executeUpdate()
                }

                // 更新文件状态为已同步
                fileId?.let {
                    conn.prepareStatement(
                        "UPDATE files SET sync_state = 'SYNCED', last_sync_time = ?, updated_at = ? WHERE id = ?"
                    ).use { stmt ->
                        val now = System.currentTimeMillis()
                        stmt.setLong(1, now)
                        stmt.setLong(2, now)
                        stmt.setLong(3, it)
                        stmt.executeUpdate()
                    }
                }

                conn.commit()
                refreshFlows()
                true
            } catch (e: Exception) {
                conn.rollback()
                throw DatabaseException("完成队列项失败: ${e.message}", e)
            } finally {
                conn.autoCommit = true
            }
        }
    }

    override fun deleteQueueItem(queueId: Long): Boolean {
        val deleted = dataSource.connection.use { conn ->
            conn.prepareStatement("DELETE FROM sync_queue WHERE id = ?").use { stmt ->
                stmt.setLong(1, queueId)
                stmt.executeUpdate()
            }
        }
        if (deleted > 0) refreshFlows()
        return deleted > 0
    }

    override fun clearCompletedQueue(): Int {
        val count = dataSource.connection.use { conn ->
            conn.prepareStatement(
                "DELETE FROM sync_queue WHERE status IN ('COMPLETED', 'CANCELLED')"
            ).use { stmt ->
                stmt.executeUpdate()
            }
        }
        if (count > 0) refreshFlows()
        return count
    }

    override fun getStats(): DatabaseStats {
        dataSource.connection.use { conn ->
            val stats = DatabaseStats(
                totalFiles = countFiles(conn),
                syncedFiles = countFilesByState(conn, SyncState.SYNCED),
                pendingUploads = countFilesByState(conn, SyncState.PENDING_UPLOAD),
                pendingDownloads = countFilesByState(conn, SyncState.PENDING_DOWNLOAD),
                conflicts = countFilesByState(conn, SyncState.CONFLICT),
                queuePending = countQueueByStatus(conn, QueueStatus.PENDING),
                queueRunning = countQueueByStatus(conn, QueueStatus.RUNNING),
                queueFailed = countQueueByStatus(conn, QueueStatus.FAILED)
            )
            return stats
        }
    }

    override fun observeFileRecords(): Flow<List<FileRecord>> = _fileRecordsFlow.asStateFlow()

    override fun observeQueue(): Flow<List<SyncQueueItem>> = _queueFlow.asStateFlow()

    private fun refreshFlows() {
        _fileRecordsFlow.value = getAllFileRecords()
        _queueFlow.value = getQueueItems()
    }

    private fun countFiles(conn: Connection): Int {
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT COUNT(*) FROM files").use { rs ->
                return if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    private fun countFilesByState(conn: Connection, state: SyncState): Int {
        conn.prepareStatement("SELECT COUNT(*) FROM files WHERE sync_state = ?").use { stmt ->
            stmt.setString(1, state.name)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    private fun countQueueByStatus(conn: Connection, status: QueueStatus): Int {
        conn.prepareStatement("SELECT COUNT(*) FROM sync_queue WHERE status = ?").use { stmt ->
            stmt.setString(1, status.name)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    private fun ResultSet.toFileRecord(): FileRecord {
        return FileRecord(
            id = getLong("id"),
            path = getString("path"),
            localMtime = getLongOrNull("local_mtime"),
            localSize = getLongOrNull("local_size"),
            localHash = getString("local_hash"),
            remoteEtag = getString("remote_etag"),
            remoteVersionId = getString("remote_version_id"),
            remoteMtime = getLongOrNull("remote_mtime"),
            remoteSize = getLongOrNull("remote_size"),
            syncState = SyncState.valueOf(getString("sync_state")),
            lastSyncTime = getLongOrNull("last_sync_time"),
            conflictStrategy = getString("conflict_strategy")?.let {
                ConflictStrategy.valueOf(it)
            }
        )
    }

    private fun ResultSet.toSyncQueueItem(): SyncQueueItem {
        return SyncQueueItem(
            id = getLong("id"),
            fileId = getLong("file_id"),
            operation = SyncOperation.valueOf(getString("operation")),
            status = QueueStatus.valueOf(getString("status")),
            progressBytes = getLong("progress_bytes"),
            totalBytes = getLong("total_bytes"),
            retryCount = getInt("retry_count"),
            errorMessage = getString("error_message"),
            createdAt = getLong("created_at"),
            updatedAt = getLong("updated_at")
        )
    }

    private fun ResultSet.getLongOrNull(column: String): Long? {
        val value = getLong(column)
        return if (wasNull()) null else value
    }
}

/**
 * 数据库管理器 - 单例
 */
object DatabaseManager {
    private var instance: Database? = null

    fun initialize(dbPath: String = DatabaseImpl.getDefaultDbPath()): Database {
        if (instance == null) {
            instance = DatabaseImpl(dbPath).apply {
                initialize()
            }
        }
        return instance!!
    }

    fun get(): Database {
        return instance ?: throw IllegalStateException("数据库未初始化，请先调用initialize()")
    }

    fun close() {
        instance?.close()
        instance = null
    }
}
