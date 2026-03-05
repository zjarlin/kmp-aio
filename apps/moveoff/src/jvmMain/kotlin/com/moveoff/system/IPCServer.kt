package com.moveoff.system

import com.moveoff.db.DatabaseManager
import com.moveoff.db.SyncState
import com.moveoff.event.EventBus
import com.moveoff.event.UIEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.Channels
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * IPC 消息类型
 */
@Serializable
sealed class IPCMessage {
    abstract val action: String
    abstract val path: String?

    @Serializable
    data class GetFileStatus(
        override val path: String
    ) : IPCMessage() {
        override val action: String = "GET_FILE_STATUS"
    }

    @Serializable
    data class TriggerSync(
        override val path: String
    ) : IPCMessage() {
        override val action: String = "TRIGGER_SYNC"
    }

    @Serializable
    data class ShowInApp(
        override val path: String
    ) : IPCMessage() {
        override val action: String = "SHOW_IN_APP"
    }

    @Serializable
    data class ResolveConflict(
        override val path: String
    ) : IPCMessage() {
        override val action: String = "RESOLVE_CONFLICT"
    }

    @Serializable
    data class GetShareLink(
        override val path: String
    ) : IPCMessage() {
        override val action: String = "GET_SHARE_LINK"
    }
}

/**
 * IPC 响应
 */
@Serializable
sealed class IPCResponse {
    @Serializable
    data class FileStatus(
        val path: String,
        val status: String,  // SYNCED, SYNCING, PENDING, CONFLICT, ERROR
        val exists: Boolean
    ) : IPCResponse()

    @Serializable
    data class Success(
        val message: String
    ) : IPCResponse()

    @Serializable
    data class Error(
        val message: String
    ) : IPCResponse()
}

/**
 * IPC 服务器 - 处理来自原生扩展的请求
 */
class IPCServer(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val json = Json { ignoreUnknownKeys = true }

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    companion object {
        private const val DEFAULT_PORT = 18476
        private val SOCKET_PATH: String by lazy {
            val userHome = System.getProperty("user.home")
            Paths.get(userHome, ".moveoff", "ipc.sock").toString()
        }
    }

    /**
     * 启动 IPC 服务器
     */
    fun start(port: Int = DEFAULT_PORT) {
        if (_isRunning.value) return

        serverJob = scope.launch {
            try {
                val osName = System.getProperty("os.name").lowercase()
                when {
                    osName.contains("mac") || osName.contains("nix") || osName.contains("nux") -> {
                        startUnixSocketServer()
                    }
                    else -> {
                        startTcpServer(port)
                    }
                }
            } catch (e: Exception) {
                println("IPC 服务器启动失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * 停止 IPC 服务器
     */
    fun stop() {
        serverJob?.cancel()
        scope.launch {
            try {
                serverSocket?.close()
                // 清理 Unix socket 文件
                val socketPath = Paths.get(SOCKET_PATH)
                if (Files.exists(socketPath)) {
                    Files.delete(socketPath)
                }
            } catch (e: Exception) {
                // 忽略
            }
            _isRunning.value = false
        }
    }

    /**
     * Unix Domain Socket 服务器（macOS/Linux）
     */
    private suspend fun startUnixSocketServer() = withContext(Dispatchers.IO) {
        try {
            // 清理旧 socket 文件
            val socketPath = Paths.get(SOCKET_PATH)
            if (Files.exists(socketPath)) {
                Files.delete(socketPath)
            }
            Files.createDirectories(socketPath.parent)

            val address = UnixDomainSocketAddress.of(socketPath)
            val serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)
            serverChannel.bind(address)

            _isRunning.value = true
            println("Unix Domain Socket 服务器已启动: $SOCKET_PATH")

            while (isActive) {
                try {
                    val clientChannel = serverChannel.accept()
                    launch { handleUnixClient(clientChannel) }
                } catch (e: Exception) {
                    if (isActive) {
                        println("接受连接失败: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("Unix Socket 服务器错误: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * TCP 服务器（Windows 或其他平台）
     */
    private suspend fun startTcpServer(port: Int) = withContext(Dispatchers.IO) {
        try {
            serverSocket = ServerSocket(port)
            _isRunning.value = true
            println("TCP IPC 服务器已启动: 127.0.0.1:$port")

            while (isActive) {
                try {
                    val clientSocket = serverSocket!!.accept()
                    launch { handleTcpClient(clientSocket) }
                } catch (e: Exception) {
                    if (isActive) {
                        println("接受连接失败: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("TCP 服务器错误: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 处理 Unix Socket 客户端
     */
    private suspend fun handleUnixClient(channel: SocketChannel) = withContext(Dispatchers.IO) {
        try {
            channel.use { ch ->
                val reader = BufferedReader(InputStreamReader(Channels.newInputStream(ch), StandardCharsets.UTF_8))
                val writer = PrintWriter(Channels.newOutputStream(ch), true)

                val request = reader.readLine() ?: return@withContext
                val response = processRequest(request)
                writer.println(response)
            }
        } catch (e: Exception) {
            println("处理 Unix 客户端错误: ${e.message}")
        }
    }

    /**
     * 处理 TCP 客户端
     */
    private suspend fun handleTcpClient(socket: Socket) = withContext(Dispatchers.IO) {
        try {
            socket.use { sock ->
                val reader = BufferedReader(InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8))
                val writer = PrintWriter(sock.getOutputStream(), true)

                val request = reader.readLine() ?: return@withContext
                val response = processRequest(request)
                writer.println(response)
            }
        } catch (e: Exception) {
            println("处理 TCP 客户端错误: ${e.message}")
        }
    }

    /**
     * 处理请求
     */
    private fun processRequest(requestJson: String): String {
        return try {
            val message = json.decodeFromString<IPCMessage>(requestJson)

            when (message) {
                is IPCMessage.GetFileStatus -> handleGetFileStatus(message)
                is IPCMessage.TriggerSync -> handleTriggerSync(message)
                is IPCMessage.ShowInApp -> handleShowInApp(message)
                is IPCMessage.ResolveConflict -> handleResolveConflict(message)
                is IPCMessage.GetShareLink -> handleGetShareLink(message)
            }
        } catch (e: Exception) {
            json.encodeToString(IPCResponse.Error("解析请求失败: ${e.message}"))
        }
    }

    /**
     * 获取文件状态
     */
    private fun handleGetFileStatus(message: IPCMessage.GetFileStatus): String {
        return try {
            val db = DatabaseManager.get()
            val record = db.getFileRecord(message.path)

            val status = when (record?.syncState) {
                SyncState.SYNCED -> "SYNCED"
                SyncState.PENDING_UPLOAD -> "PENDING_UPLOAD"
                SyncState.PENDING_DOWNLOAD -> "PENDING_DOWNLOAD"
                SyncState.CONFLICT -> "CONFLICT"
                SyncState.ERROR -> "ERROR"
                null -> "NOT_TRACKED"
            }

            val file = java.io.File(message.path)
            json.encodeToString(IPCResponse.FileStatus(
                path = message.path,
                status = status,
                exists = file.exists()
            ))
        } catch (e: Exception) {
            json.encodeToString(IPCResponse.Error("获取文件状态失败: ${e.message}"))
        }
    }

    /**
     * 触发同步
     */
    private fun handleTriggerSync(message: IPCMessage.TriggerSync): String {
        scope.launch {
            EventBus.emit(UIEvent.SyncStarted(1, "Finder/Explorer 触发同步"))
            // TODO: 触发具体文件的同步
        }
        return json.encodeToString(IPCResponse.Success("同步已触发"))
    }

    /**
     * 在应用中显示
     */
    private fun handleShowInApp(message: IPCMessage.ShowInApp): String {
        scope.launch {
            EventBus.emit(UIEvent.WindowShouldShow)
            // TODO: 选中该文件
        }
        return json.encodeToString(IPCResponse.Success("窗口已显示"))
    }

    /**
     * 解决冲突
     */
    private fun handleResolveConflict(message: IPCMessage.ResolveConflict): String {
        scope.launch {
            EventBus.emit(UIEvent.WindowShouldShow)
            // TODO: 打开冲突解决对话框
        }
        return json.encodeToString(IPCResponse.Success("冲突解决对话框已打开"))
    }

    /**
     * 获取共享链接
     */
    private fun handleGetShareLink(message: IPCMessage.GetShareLink): String {
        // TODO: 实现获取共享链接
        return json.encodeToString(IPCResponse.Error("共享链接功能暂未实现"))
    }

    /**
     * 获取 Socket 路径（用于 Finder Extension 连接）
     */
    fun getSocketPath(): String = SOCKET_PATH

    /**
     * 获取 TCP 端口（用于 Windows）
     */
    fun getTcpPort(): Int = DEFAULT_PORT
}

/**
 * IPC 服务器管理器 - 单例
 */
object IPCServerManager {
    private var instance: IPCServer? = null

    fun start(): IPCServer {
        if (instance == null) {
            instance = IPCServer().apply { start() }
        }
        return instance!!
    }

    fun get(): IPCServer {
        return instance ?: throw IllegalStateException("IPC 服务器未启动")
    }

    fun stop() {
        instance?.stop()
        instance = null
    }
}
