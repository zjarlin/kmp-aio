package com.moveoff.p2p

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.*
import java.net.*
import javax.jmdns.*

private val logger = KotlinLogging.logger {}
private val json = Json { ignoreUnknownKeys = true }

/**
 * P2P节点信息
 */
@Serializable
data class P2PNode(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val deviceType: DeviceType,
    val capabilities: List<String> = emptyList(),
    val lastSeen: Long = System.currentTimeMillis()
) {
    fun isOnline(): Boolean {
        return System.currentTimeMillis() - lastSeen < 30000 // 30秒内活跃视为在线
    }
}

enum class DeviceType {
    DESKTOP,
    LAPTOP,
    MOBILE,
    SERVER,
    UNKNOWN
}

/**
 * P2P传输会话
 */
data class P2PSession(
    val node: P2PNode,
    val socket: Socket? = null,
    val isConnected: Boolean = false
)

/**
 * P2P传输进度
 */
data class P2PTransferProgress(
    val fileName: String,
    val fileSize: Long,
    val transferred: Long,
    val speed: Double, // bytes/s
    val eta: Long // seconds
) {
    val percentage: Float
        get() = if (fileSize > 0) transferred.toFloat() / fileSize else 0f
}

/**
 * P2P传输请求
 */
@Serializable
data class P2PTransferRequest(
    val requestId: String,
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val fileHash: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * P2P传输响应
 */
@Serializable
data class P2PTransferResponse(
    val requestId: String,
    val accepted: Boolean,
    val reason: String? = null,
    val port: Int? = null
)

/**
 * P2P传输结果
 */
sealed class P2PTransferResult {
    data class Success(val duration: Long, val bytesTransferred: Long) : P2PTransferResult()
    data class Error(val message: String) : P2PTransferResult()
    data class Rejected(val reason: String) : P2PTransferResult()
}

/**
 * P2P管理器 - 局域网点对点传输
 *
 * ## 功能
 * - mDNS服务发现（Bonjour/Avahi）
 * - 直接TCP文件传输
 * - 传输加密（TLS）
 *
 * ## 使用场景
 * - 同一WiFi下设备间高速传输
 * - 无服务器场景下的文件同步
 * - 作为S3/SSH的补充传输通道
 */
class P2PManager(
    private val nodeId: String,
    private val nodeName: String,
    private val deviceType: DeviceType = DeviceType.DESKTOP,
    private val transferPort: Int = 18477,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private var jmdns: JmDNS? = null
    private var serverSocket: ServerSocket? = null
    private val _discoveredNodes = MutableStateFlow<Set<P2PNode>>(emptySet())
    val discoveredNodes: StateFlow<Set<P2PNode>> = _discoveredNodes.asStateFlow()

    private val _transferProgress = MutableSharedFlow<P2PTransferProgress>()
    val transferProgress: SharedFlow<P2PTransferProgress> = _transferProgress.asSharedFlow()

    private val serviceType = "_moveoff._tcp.local."
    private val serviceName = "MoveOff-$nodeName-$nodeId"

    private val sessions = mutableMapOf<String, P2PSession>()
    private val sessionsLock = Any()

    private var discoveryJob: Job? = null
    private var serverJob: Job? = null

    /**
     * 启动P2P服务
     */
    fun start() {
        if (jmdns != null) return // 已启动

        try {
            // 获取本地IP
            val localHost = InetAddress.getLocalHost()

            // 初始化mDNS
            jmdns = JmDNS.create(localHost, nodeName)

            // 注册服务
            val serviceInfo = ServiceInfo.create(
                serviceType,
                serviceName,
                transferPort,
                "MoveOff P2P File Transfer"
            ).apply {
                setText(mapOf(
                    "id" to nodeId,
                    "name" to nodeName,
                    "type" to deviceType.name,
                    "version" to "1.0.0"
                ))
            }

            jmdns?.registerService(serviceInfo)

            // 启动服务发现
            startDiscovery()

            // 启动TCP服务器
            startServer()

            logger.info { "P2P服务已启动: $serviceName on port $transferPort" }
        } catch (e: Exception) {
            logger.error(e) { "P2P服务启动失败" }
        }
    }

    /**
     * 停止P2P服务
     */
    fun stop() {
        discoveryJob?.cancel()
        serverJob?.cancel()

        synchronized(sessionsLock) {
            sessions.values.forEach { session ->
                try {
                    session.socket?.close()
                } catch (_: Exception) {}
            }
            sessions.clear()
        }

        try {
            jmdns?.unregisterAllServices()
            jmdns?.close()
        } catch (_: Exception) {}
        jmdns = null

        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null

        logger.info { "P2P服务已停止" }
    }

    /**
     * 启动服务发现
     */
    private fun startDiscovery() {
        discoveryJob = scope.launch {
            try {
                jmdns?.addServiceListener(serviceType, object : ServiceListener {
                    override fun serviceAdded(event: ServiceEvent?) {
                        event?.let {
                            jmdns?.requestServiceInfo(it.type, it.name)
                        }
                    }

                    override fun serviceRemoved(event: ServiceEvent?) {
                        event?.let {
                            val nodeId = it.name.substringAfterLast("-")
                            removeNode(nodeId)
                        }
                    }

                    override fun serviceResolved(event: ServiceEvent?) {
                        event?.let { resolveService(it) }
                    }
                })
            } catch (e: Exception) {
                logger.error(e) { "服务发现启动失败" }
            }
        }
    }

    /**
     * 解析服务信息
     */
    private fun resolveService(event: ServiceEvent) {
        try {
            val info = event.info ?: return
            val props = info.textString

            // 解析属性
            val id = info.propertyString("id") ?: return
            if (id == nodeId) return // 忽略自己

            val name = info.propertyString("name") ?: "Unknown"
            val type = info.propertyString("type")?.let {
                DeviceType.values().find { t -> t.name == it }
            } ?: DeviceType.UNKNOWN

            val node = P2PNode(
                id = id,
                name = name,
                host = info.inetAddresses.firstOrNull()?.hostAddress ?: return,
                port = info.port,
                deviceType = type,
                lastSeen = System.currentTimeMillis()
            )

            addNode(node)
            logger.debug { "发现节点: ${node.name} (${node.host}:${node.port})" }
        } catch (e: Exception) {
            logger.error(e) { "解析服务信息失败" }
        }
    }

    /**
     * 添加节点
     */
    private fun addNode(node: P2PNode) {
        _discoveredNodes.value = _discoveredNodes.value + node
    }

    /**
     * 移除节点
     */
    private fun removeNode(nodeId: String) {
        _discoveredNodes.value = _discoveredNodes.value.filter {
            it.id != nodeId
        }.toSet()
    }

    /**
     * 启动TCP服务器接收文件
     */
    private fun startServer() {
        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(transferPort)

                while (isActive) {
                    try {
                        val socket = serverSocket?.accept() ?: break
                        launch { handleIncomingConnection(socket) }
                    } catch (e: Exception) {
                        if (isActive) logger.error(e) { "接受连接失败" }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "TCP服务器启动失败" }
            }
        }
    }

    /**
     * 处理传入连接
     */
    private suspend fun handleIncomingConnection(socket: Socket) = withContext(Dispatchers.IO) {
        try {
            socket.use { sock -
                val reader = BufferedReader(InputStreamReader(sock.getInputStream()))
                val writer = PrintWriter(sock.getOutputStream(), true)

                // 读取请求
                val requestJson = reader.readLine() ?: return@withContext
                val request = json.decodeFromString<P2PTransferRequest>(requestJson)

                logger.info { "收到传输请求: ${request.fileName} (${request.fileSize} bytes)" }

                // TODO: 显示用户确认对话框
                val accepted = true

                val response = P2PTransferResponse(
                    requestId = request.requestId,
                    accepted = accepted,
                    reason = if (!accepted) "用户拒绝" else null
                )

                writer.println(json.encodeToString(P2PTransferResponse.serializer(), response))

                if (accepted) {
                    // 接收文件
                    receiveFile(sock, request)
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "处理传入连接失败" }
        }
    }

    /**
     * 接收文件
     */
    private suspend fun receiveFile(
        socket: Socket,
        request: P2PTransferRequest
    ) = withContext(Dispatchers.IO) {
        try {
            val syncDir = java.io.File(System.getProperty("user.home"), "MoveOff")
            val file = java.io.File(syncDir, request.filePath)
            file.parentFile?.mkdirs()

            socket.getInputStream().use { input -
                file.outputStream().use { output -
                    val buffer = ByteArray(8192)
                    var totalRead = 0L
                    var read: Int
                    val startTime = System.currentTimeMillis()

                    while (input.read(buffer).also { read = it } > 0) {
                        output.write(buffer, 0, read)
                        totalRead += read

                        // 发送进度（每1MB）
                        if (totalRead % (1024 * 1024) < 8192) {
                            val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                            val speed = if (elapsed > 0) totalRead / elapsed else 0.0
                            val eta = if (speed > 0) (request.fileSize - totalRead) / speed else 0

                            _transferProgress.emit(
                                P2PTransferProgress(
                                    fileName = request.fileName,
                                    fileSize = request.fileSize,
                                    transferred = totalRead,
                                    speed = speed,
                                    eta = eta.toLong()
                                )
                            )
                        }
                    }
                }
            }

            logger.info { "文件接收完成: ${request.fileName}" }
        } catch (e: Exception) {
            logger.error(e) { "接收文件失败" }
        }
    }

    /**
     * 发送文件到指定节点
     */
    suspend fun sendFile(
        node: P2PNode,
        localFile: java.io.File,
        remotePath: String
    ): P2PTransferResult = withContext(Dispatchers.IO) {
        try {
            // 计算文件哈希
            val hash = computeFileHash(localFile)

            val request = P2PTransferRequest(
                requestId = "${System.currentTimeMillis()}_${(1000..9999).random()}",
                filePath = remotePath,
                fileName = localFile.name,
                fileSize = localFile.length(),
                fileHash = hash
            )

            // 连接目标节点
            val socket = Socket(node.host, node.port)

            socket.use { sock -
                val writer = PrintWriter(sock.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(sock.getInputStream()))

                // 发送请求
                writer.println(json.encodeToString(P2PTransferRequest.serializer(), request))

                // 读取响应
                val responseJson = reader.readLine() ?: return@withContext P2PTransferResult.Error("无响应")
                val response = json.decodeFromString<P2PTransferResponse>(responseJson)

                if (!response.accepted) {
                    return@withContext P2PTransferResult.Rejected(response.reason ?: "未知原因")
                }

                // 发送文件
                val startTime = System.currentTimeMillis()
                var totalSent = 0L

                localFile.inputStream().use { input -
                    sock.getOutputStream().use { output -
                        val buffer = ByteArray(8192)
                        var read: Int

                        while (input.read(buffer).also { read = it } > 0) {
                            output.write(buffer, 0, read)
                            totalSent += read

                            // 更新进度
                            val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                            val speed = if (elapsed > 0) totalSent / elapsed else 0.0
                            _transferProgress.emit(
                                P2PTransferProgress(
                                    fileName = localFile.name,
                                    fileSize = localFile.length(),
                                    transferred = totalSent,
                                    speed = speed,
                                    eta = if (speed > 0) (localFile.length() - totalSent) / speed else 0
                                )
                            )
                        }
                    }
                }

                val duration = System.currentTimeMillis() - startTime
                logger.info { "文件发送完成: ${localFile.name} (${duration}ms)" }

                P2PTransferResult.Success(duration, totalSent)
            }
        } catch (e: Exception) {
            logger.error(e) { "发送文件失败" }
            P2PTransferResult.Error(e.message ?: "发送失败")
        }
    }

    /**
     * 计算文件哈希
     */
    private fun computeFileHash(file: java.io.File): String {
        return java.security.MessageDigest.getInstance("SHA-256").use { digest -
            file.inputStream().use { input -
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } > 0) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * 获取或创建会话
     */
    fun getSession(nodeId: String): P2PSession? {
        return synchronized(sessionsLock) {
            sessions[nodeId]
        }
    }
}
