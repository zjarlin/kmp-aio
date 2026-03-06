package com.moveoff.sync

import com.moveoff.state.AppStateManager
import com.moveoff.state.SyncStatus
import com.moveoff.team.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.NetworkInterface

private val logger = KotlinLogging.logger {}

/**
 * 智能同步管理器
 *
 * 根据网络类型、电量、时间等条件自动调整同步策略
 */
class SmartSyncManager(
    private val settings: SmartSyncSettings = SmartSyncSettings(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val _currentPolicy = MutableStateFlow<NetworkSyncPolicy?>(null)
    val currentPolicy: StateFlow<NetworkSyncPolicy?> = _currentPolicy.asStateFlow()

    private val _currentNetworkType = MutableStateFlow(NetworkType.UNKNOWN)
    val currentNetworkType: StateFlow<NetworkType> = _currentNetworkType.asStateFlow()

    private val _shouldSync = MutableStateFlow(false)
    val shouldSync: StateFlow<Boolean> = _shouldSync.asStateFlow()

    private var monitorJob: Job? = null

    init {
        startMonitoring()
    }

    /**
     * 启动网络监控
     */
    private fun startMonitoring() {
        monitorJob = scope.launch {
            while (isActive) {
                checkNetworkAndUpdatePolicy()
                delay(30000) // 每30秒检查一次
            }
        }
    }

    /**
     * 停止监控
     */
    fun stop() {
        monitorJob?.cancel()
    }

    /**
     * 检查网络并更新策略
     */
    private suspend fun checkNetworkAndUpdatePolicy() {
        val networkType = detectNetworkType()
        _currentNetworkType.value = networkType

        val policy = settings.policies[networkType] ?: settings.policies[NetworkType.UNKNOWN]
        ?: NetworkSyncPolicy(networkType, true, SyncDirection.BOTH)

        _currentPolicy.value = policy

        // 检查是否应该同步
        val shouldSyncNow = evaluateShouldSync(policy)
        _shouldSync.value = shouldSyncNow

        if (shouldSyncNow && !policy.autoSyncEnabled) {
            // 网络条件允许但策略禁用
            logger.info { "网络类型 $networkType 自动同步已禁用" }
        }
    }

    /**
     * 评估是否应该同步
     */
    private fun evaluateShouldSync(policy: NetworkSyncPolicy): Boolean {
        // 1. 检查策略是否启用
        if (!policy.autoSyncEnabled) return false

        // 2. 检查计划时间
        if (settings.scheduleEnabled) {
            val currentHour = java.time.LocalTime.now().hour
            if (currentHour !in settings.scheduleStartHour until settings.scheduleEndHour) {
                return false
            }
        }

        // 3. 检查电量（移动端）
        if (settings.pauseOnBatterySaver) {
            val batteryLevel = getBatteryLevel()
            if (batteryLevel < settings.batteryThreshold) {
                logger.info { "电量低 ($batteryLevel%)，暂停同步" }
                return false
            }
        }

        return true
    }

    /**
     * 检测当前网络类型
     */
    private fun detectNetworkType(): NetworkType {
        return try {
            // 获取活动网络接口
            val interfaces = NetworkInterface.getNetworkInterfaces()
            var hasWifi = false
            var hasEthernet = false
            var hasMobile = false

            interfaces.asSequence().forEach { iface ->
                if (iface.isUp && !iface.isLoopback) {
                    when {
                        iface.name.startsWith("wl") || iface.displayName.contains("Wi-Fi", true) -> hasWifi = true
                        iface.name.startsWith("en") || iface.name.startsWith("eth") -> hasEthernet = true
                        iface.name.startsWith("ppp") || iface.displayName.contains("Mobile", true) -> hasMobile = true
                    }
                }
            }

            when {
                hasEthernet -> NetworkType.ETHERNET
                hasWifi -> NetworkType.WIFI
                hasMobile -> NetworkType.MOBILE
                else -> NetworkType.UNKNOWN
            }
        } catch (e: Exception) {
            logger.error(e) { "检测网络类型失败" }
            NetworkType.UNKNOWN
        }
    }

    /**
     * 获取电量水平（简化实现，实际应该调用系统API）
     */
    private fun getBatteryLevel(): Int {
        // TODO: 实现跨平台电量检测
        // macOS: pmset -g batt
        // Linux: /sys/class/power_supply/BAT0/capacity
        // Windows: WMI查询
        return 100 // 默认返回100%
    }

    /**
     * 检查是否应该传输大文件
     */
    fun shouldTransferLargeFile(fileSize: Long): LargeFileBehavior {
        val policy = _currentPolicy.value ?: return LargeFileBehavior.ASK

        return when {
            fileSize < policy.largeFileThreshold -> LargeFileBehavior.SYNC
            else -> policy.largeFileBehavior
        }
    }

    /**
     * 获取当前带宽限制
     */
    fun getBandwidthLimit(): Long? {
        return _currentPolicy.value?.bandwidthLimit
    }

    /**
     * 获取当前同步方向
     */
    fun getSyncDirection(): SyncDirection {
        return _currentPolicy.value?.syncDirection ?: SyncDirection.BOTH
    }

    /**
     * 更新设置
     */
    fun updateSettings(newSettings: SmartSyncSettings) {
        // 设置新值
        scope.launch {
            checkNetworkAndUpdatePolicy()
        }
    }

    /**
     * 手动触发同步检查
     */
    fun forceCheck() {
        scope.launch {
            checkNetworkAndUpdatePolicy()
        }
    }

    /**
     * 获取网络建议
     */
    fun getNetworkAdvice(): String {
        val networkType = _currentNetworkType.value
        val policy = _currentPolicy.value

        return when {
            networkType == NetworkType.METERED -> "⚠️ 当前使用按流量计费网络，同步已暂停"
            networkType == NetworkType.MOBILE -> "📱 当前使用移动网络，建议仅在WiFi下同步大文件"
            policy?.autoSyncEnabled == false -> "⏸️ 当前网络类型自动同步已禁用"
            settings.scheduleEnabled && !_shouldSync.value -> "⏰ 当前不在计划同步时间段内"
            else -> "✅ 网络条件良好，可以正常同步"
        }
    }
}

/**
 * 网络建议卡片数据
 */
data class NetworkAdvice(
    val icon: String,
    val title: String,
    val message: String,
    val action: String? = null
)