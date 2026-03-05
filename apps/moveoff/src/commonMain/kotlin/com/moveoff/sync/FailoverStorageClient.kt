package com.moveoff.sync

import com.moveoff.event.EventBus
import com.moveoff.event.UIEvent
import com.moveoff.sync.api.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.seconds

/**
 * 存储后端类型
 */
enum class StorageBackendType {
    S3,
    SSH
}

/**
 * 存储后端状态
 */
data class StorageBackendState(
    val type: StorageBackendType,
    val isHealthy: Boolean,
    val lastError: String? = null,
    val lastCheckTime: Long = System.currentTimeMillis()
)

/**
 * 自动故障转移存储客户端
 *
 * 管理多个存储后端（S3优先，SSH备用），自动检测连接健康状态，
 * 在主后端不可用时自动切换到备用后端。
 */
class FailoverStorageClient(
    private val primaryClient: StorageClient,
    private val primaryType: StorageBackendType,
    private val fallbackClient: StorageClient? = null,
    private val fallbackType: StorageBackendType? = null,
    private val healthCheckIntervalMs: Long = 30000L, // 30秒检查一次
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : StorageClient {

    private val _currentBackend = MutableStateFlow(primaryType)
    val currentBackend: StateFlow<StorageBackendType> = _currentBackend.asStateFlow()

    private val _backendStates = MutableStateFlow<Map<StorageBackendType, StorageBackendState>>(emptyMap())
    val backendStates: StateFlow<Map<StorageBackendType, StorageBackendState>> = _backendStates.asStateFlow()

    private val _isFailoverActive = MutableStateFlow(false)
    val isFailoverActive: StateFlow<Boolean> = _isFailoverActive.asStateFlow()

    private var healthCheckJob: Job? = null
    private val mutex = Any()

    init {
        // 初始化状态
        _backendStates.value = mapOf(
            primaryType to StorageBackendState(primaryType, true),
            fallbackType to StorageBackendState(fallbackType ?: StorageBackendType.SSH, true)
        )
    }

    /**
     * 启动健康检查
     */
    fun startHealthCheck() {
        healthCheckJob?.cancel()
        healthCheckJob = scope.launch {
            while (isActive) {
                checkHealth()
                delay(healthCheckIntervalMs)
            }
        }
    }

    /**
     * 停止健康检查
     */
    fun stopHealthCheck() {
        healthCheckJob?.cancel()
        healthCheckJob = null
    }

    /**
     * 检查所有后端健康状态
     */
    private suspend fun checkHealth() {
        // 检查主后端
        val primaryHealthy = try {
            primaryClient.testConnection()
        } catch (e: Exception) {
            false
        }

        updateBackendState(primaryType, primaryHealthy)

        // 检查备用后端
        fallbackType?.let { fallback ->
            val fallbackHealthy = fallbackClient?.let { client ->
                try {
                    client.testConnection()
                } catch (e: Exception) {
                    false
                }
            } ?: false

            updateBackendState(fallback, fallbackHealthy)
        }

        // 检查是否需要切换
        evaluateFailover()
    }

    /**
     * 更新后端状态
     */
    private fun updateBackendState(type: StorageBackendType, isHealthy: Boolean) {
        synchronized(mutex) {
            val currentStates = _backendStates.value.toMutableMap()
            val currentState = currentStates[type]

            if (currentState?.isHealthy != isHealthy) {
                // 状态发生变化
                currentStates[type] = StorageBackendState(
                    type = type,
                    isHealthy = isHealthy,
                    lastCheckTime = System.currentTimeMillis()
                )
                _backendStates.value = currentStates

                // 触发状态变化事件
                val event = if (isHealthy) {
                    UIEvent.StorageBackendRecovered(type.name)
                } else {
                    UIEvent.StorageBackendFailed(type.name)
                }
                EventBus.emit(event)
            }
        }
    }

    /**
     * 评估是否需要故障转移
     */
    private fun evaluateFailover() {
        synchronized(mutex) {
            val current = _currentBackend.value
            val states = _backendStates.value

            val primaryState = states[primaryType]
            val fallbackState = fallbackType?.let { states[it] }

            when (current) {
                primaryType -> {
                    // 当前使用主后端，如果主后端不健康且备用后端健康，则切换
                    if (primaryState?.isHealthy == false && fallbackState?.isHealthy == true) {
                        switchToFallback()
                    }
                }
                fallbackType -> {
                    // 当前使用备用后端，如果主后端恢复，则切回
                    if (primaryState?.isHealthy == true) {
                        switchToPrimary()
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * 切换到备用后端
     */
    private fun switchToFallback() {
        fallbackType?.let { fallback ->
            _currentBackend.value = fallback
            _isFailoverActive.value = true
            EventBus.emit(UIEvent.FailoverActivated(primaryType.name, fallback.name))
        }
    }

    /**
     * 切换回主后端
     */
    private fun switchToPrimary() {
        _currentBackend.value = primaryType
        _isFailoverActive.value = false
        EventBus.emit(UIEvent.FailoverRecovered(primaryType.name))
    }

    /**
     * 手动切换到指定后端
     */
    fun switchBackend(type: StorageBackendType): Boolean {
        return synchronized(mutex) {
            val states = _backendStates.value
            val state = states[type]

            if (state?.isHealthy == true) {
                val previous = _currentBackend.value
                _currentBackend.value = type
                _isFailoverActive.value = (type != primaryType)

                if (previous != type) {
                    EventBus.emit(UIEvent.FailoverActivated(previous.name, type.name))
                }
                true
            } else {
                false
            }
        }
    }

    /**
     * 获取当前活动的存储客户端
     */
    private fun getActiveClient(): StorageClient {
        return when (_currentBackend.value) {
            primaryType -> primaryClient
            fallbackType -> fallbackClient
            else -> primaryClient
        } ?: primaryClient
    }

    // ========== StorageClient 接口实现 ==========

    override suspend fun testConnection(): Boolean {
        return getActiveClient().testConnection()
    }

    override suspend fun listObjects(prefix: String?): List<RemoteObject> {
        return withRetry { client ->
            client.listObjects(prefix)
        }
    }

    override suspend fun uploadObject(
        localPath: String,
        remotePath: String,
        progress: (Long, Long) -> Unit
    ): UploadResult {
        return withRetry { client ->
            client.uploadObject(localPath, remotePath, progress)
        }
    }

    override suspend fun downloadObject(
        remotePath: String,
        localPath: String,
        progress: (Long, Long) -> Unit
    ): DownloadResult {
        return withRetry { client ->
            client.downloadObject(remotePath, localPath, progress)
        }
    }

    override suspend fun deleteObject(remotePath: String): Boolean {
        return withRetry { client ->
            client.deleteObject(remotePath)
        }
    }

    override suspend fun getObjectMetadata(remotePath: String): RemoteObject? {
        return withRetry { client ->
            client.getObjectMetadata(remotePath)
        }
    }

    /**
     * 带重试的操作执行器
     * 如果操作失败，尝试切换到备用后端重试
     */
    private suspend fun <T> withRetry(
        operation: suspend (StorageClient) -> T
    ): T {
        val activeClient = getActiveClient()

        return try {
            operation(activeClient)
        } catch (e: Exception) {
            // 操作失败，标记当前后端为不健康
            updateBackendState(_currentBackend.value, false)

            // 尝试切换到备用后端
            evaluateFailover()

            // 如果有备用后端，重试一次
            val fallback = getActiveClient()
            if (fallback != activeClient) {
                operation(fallback)
            } else {
                throw e
            }
        }
    }

    /**
     * 获取存储后端统计信息
     */
    fun getStats(): FailoverStats {
        return FailoverStats(
            currentBackend = _currentBackend.value,
            isFailoverActive = _isFailoverActive.value,
            backendStates = _backendStates.value
        )
    }

    /**
     * 关闭所有连接
     */
    fun close() {
        stopHealthCheck()
        scope.cancel()
    }
}

/**
 * 故障转移统计
 */
data class FailoverStats(
    val currentBackend: StorageBackendType,
    val isFailoverActive: Boolean,
    val backendStates: Map<StorageBackendType, StorageBackendState>
)

/**
 * 故障转移管理器 - 单例
 */
object FailoverStorageManager {
    private var instance: FailoverStorageClient? = null

    fun initialize(
        primaryClient: StorageClient,
        primaryType: StorageBackendType,
        fallbackClient: StorageClient? = null,
        fallbackType: StorageBackendType? = null
    ): FailoverStorageClient {
        if (instance == null) {
            instance = FailoverStorageClient(
                primaryClient = primaryClient,
                primaryType = primaryType,
                fallbackClient = fallbackClient,
                fallbackType = fallbackType
            ).apply {
                startHealthCheck()
            }
        }
        return instance!!
    }

    fun get(): FailoverStorageClient {
        return instance ?: throw IllegalStateException("FailoverStorageClient未初始化")
    }

    fun stop() {
        instance?.close()
        instance = null
    }
}
