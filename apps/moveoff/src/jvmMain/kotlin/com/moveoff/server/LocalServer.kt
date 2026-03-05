package com.moveoff.server

import com.moveoff.db.Database
import com.moveoff.db.DatabaseManager
import com.moveoff.event.EventBus
import com.moveoff.event.UIEvent
import com.moveoff.state.AppStateManager
import com.moveoff.sync.SyncEngine
import com.moveoff.sync.SyncEngineManager
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * MoveOff 本地服务器
 *
 * 功能：
 * 1. 提供HTTP API供UI调用（即使UI是Compose也可以走HTTP）
 * 2. WebSocket推送实时同步状态
 * 3. 接收外部触发（如右键菜单、命令行）
 * 4. 提供本地文件访问接口
 *
 * 监听地址：127.0.0.1:18475 （随机高端口，避免冲突）
 */
class LocalServer(
    private val port: Int = 18475,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
    private val connections = mutableSetOf<DefaultWebSocketServerSession>()
    private var isRunning = false

    /**
     * 启动服务器
     */
    fun start() {
        if (isRunning) return

        server = embeddedServer(CIO, port = port) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                })
            }

            install(CORS) {
                anyHost()
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Delete)
                allowHeader(HttpHeaders.ContentType)
            }

            install(WebSockets)

            routing {
                // 健康检查
                get("/health") {
                    call.respond(mapOf(
                        "status" to "ok",
                        "version" to "1.0.0",
                        "timestamp" to System.currentTimeMillis()
                    ))
                }

                // 获取同步状态
                get("/api/sync/status") {
                    val state = AppStateManager.currentState
                    call.respond(SyncStatusResponse(
                        status = state.syncStatus.name,
                        progress = state.overallProgress,
                        currentOperation = state.currentOperation,
                        pendingUploads = state.pendingUploads,
                        pendingDownloads = state.pendingDownloads,
                        conflictCount = state.conflictCount,
                        isOnline = state.isOnline,
                        lastSyncTime = state.lastSyncTime
                    ))
                }

                // 触发同步
                post("/api/sync/trigger") {
                    scope.launch {
                        try {
                            val syncEngine = SyncEngineManager.get()
                            syncEngine.syncNow()
                            call.respond(mapOf("success" to true))
                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("success" to false, "error" to e.message)
                            )
                        }
                    }
                }

                // 暂停/恢复同步
                post("/api/sync/pause") {
                    val syncEngine = SyncEngineManager.get()
                    syncEngine.pause()
                    call.respond(mapOf("success" to true, "paused" to true))
                }

                post("/api/sync/resume") {
                    val syncEngine = SyncEngineManager.get()
                    syncEngine.resume()
                    call.respond(mapOf("success" to true, "paused" to false))
                }

                // 获取文件列表
                get("/api/files") {
                    val db = DatabaseManager.get()
                    val files = db.getAllFileRecords().map { record ->
                        FileInfoResponse(
                            path = record.path,
                            localSize = record.localSize,
                            remoteSize = record.remoteSize,
                            syncState = record.syncState.name,
                            lastSyncTime = record.lastSyncTime
                        )
                    }
                    call.respond(files)
                }

                // 获取冲突列表
                get("/api/conflicts") {
                    val db = DatabaseManager.get()
                    val conflicts = db.getFileRecordsByState(com.moveoff.db.SyncState.CONFLICT)
                        .map { record ->
                            ConflictResponse(
                                path = record.path,
                                localSize = record.localSize ?: 0,
                                remoteSize = record.remoteSize ?: 0
                            )
                        }
                    call.respond(conflicts)
                }

                // 解决冲突
                post("/api/conflicts/resolve") {
                    val request = call.receive<ResolveConflictRequest>()
                    val db = DatabaseManager.get()
                    val success = db.resolveConflict(
                        request.path,
                        com.moveoff.db.ConflictResolution.valueOf(request.resolution)
                    )
                    call.respond(mapOf("success" to success))
                }

                // 获取同步队列
                get("/api/queue") {
                    val db = DatabaseManager.get()
                    val queue = db.getQueueItems().map { item ->
                        QueueItemResponse(
                            id = item.id,
                            operation = item.operation.name,
                            status = item.status.name,
                            progress = if (item.totalBytes > 0) {
                                item.progressBytes.toFloat() / item.totalBytes
                            } else 0f,
                            retryCount = item.retryCount
                        )
                    }
                    call.respond(queue)
                }

                // 获取数据库统计
                get("/api/stats") {
                    val db = DatabaseManager.get()
                    val stats = db.getStats()
                    call.respond(StatsResponse(
                        totalFiles = stats.totalFiles,
                        syncedFiles = stats.syncedFiles,
                        pendingUploads = stats.pendingUploads,
                        pendingDownloads = stats.pendingDownloads,
                        conflicts = stats.conflicts,
                        queuePending = stats.queuePending,
                        queueRunning = stats.queueRunning,
                        queueFailed = stats.queueFailed
                    ))
                }

                // WebSocket - 实时推送状态
                webSocket("/ws") {
                    connections.add(this)
                    try {
                        // 发送初始状态
                        val state = AppStateManager.currentState
                        send(Json.encodeToString(SyncStatusResponse.serializer(), SyncStatusResponse(
                            status = state.syncStatus.name,
                            progress = state.overallProgress,
                            currentOperation = state.currentOperation,
                            pendingUploads = state.pendingUploads,
                            pendingDownloads = state.pendingDownloads,
                            conflictCount = state.conflictCount,
                            isOnline = state.isOnline,
                            lastSyncTime = state.lastSyncTime
                        )))

                        // 保持连接
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                // 处理客户端消息（如心跳）
                                if (text == "ping") {
                                    send("pong")
                                }
                            }
                        }
                    } finally {
                        connections.remove(this)
                    }
                }
            }
        }.start(wait = false)

        isRunning = true

        // 启动WebSocket广播
        startWebSocketBroadcast()

        println("本地服务器已启动: http://127.0.0.1:$port")
    }

    /**
     * 停止服务器
     */
    fun stop() {
        server?.stop(1000, 2000)
        isRunning = false
        connections.clear()
    }

    /**
     * WebSocket广播 - 将状态变化推送到所有连接
     */
    private fun startWebSocketBroadcast() {
        scope.launch {
            AppStateManager.state.collectLatest { state ->
                val message = Json.encodeToString(SyncStatusResponse.serializer(), SyncStatusResponse(
                    status = state.syncStatus.name,
                    progress = state.overallProgress,
                    currentOperation = state.currentOperation,
                    pendingUploads = state.pendingUploads,
                    pendingDownloads = state.pendingDownloads,
                    conflictCount = state.conflictCount,
                    isOnline = state.isOnline,
                    lastSyncTime = state.lastSyncTime
                ))

                connections.forEach { session ->
                    try {
                        session.send(message)
                    } catch (e: Exception) {
                        // 连接可能已关闭，忽略错误
                    }
                }
            }
        }
    }

    /**
     * 广播消息给所有WebSocket客户端
     */
    fun broadcast(message: String) {
        scope.launch {
            connections.forEach { session ->
                try {
                    session.send(message)
                } catch (e: Exception) {
                    // 忽略错误
                }
            }
        }
    }
}

// ========== 请求/响应数据类 ==========

@Serializable
data class SyncStatusResponse(
    val status: String,
    val progress: Float,
    val currentOperation: String?,
    val pendingUploads: Int,
    val pendingDownloads: Int,
    val conflictCount: Int,
    val isOnline: Boolean,
    val lastSyncTime: Long?
)

@Serializable
data class FileInfoResponse(
    val path: String,
    val localSize: Long?,
    val remoteSize: Long?,
    val syncState: String,
    val lastSyncTime: Long?
)

@Serializable
data class ConflictResponse(
    val path: String,
    val localSize: Long,
    val remoteSize: Long
)

@Serializable
data class ResolveConflictRequest(
    val path: String,
    val resolution: String  // USE_LOCAL, USE_REMOTE, KEEP_BOTH
)

@Serializable
data class QueueItemResponse(
    val id: Long,
    val operation: String,
    val status: String,
    val progress: Float,
    val retryCount: Int
)

@Serializable
data class StatsResponse(
    val totalFiles: Int,
    val syncedFiles: Int,
    val pendingUploads: Int,
    val pendingDownloads: Int,
    val conflicts: Int,
    val queuePending: Int,
    val queueRunning: Int,
    val queueFailed: Int
)

/**
 * 本地服务器管理器 - 单例
 */
object LocalServerManager {
    private var instance: LocalServer? = null

    fun start(port: Int = 18475): LocalServer {
        if (instance == null) {
            instance = LocalServer(port).apply { start() }
        }
        return instance!!
    }

    fun stop() {
        instance?.stop()
        instance = null
    }

    fun get(): LocalServer {
        return instance ?: throw IllegalStateException("本地服务器未启动")
    }
}
