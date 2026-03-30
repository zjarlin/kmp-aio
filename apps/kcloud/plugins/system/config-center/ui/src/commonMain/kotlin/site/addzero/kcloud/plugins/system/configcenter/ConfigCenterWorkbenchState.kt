package site.addzero.kcloud.plugins.system.configcenter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueWriteRequest

@Single
class ConfigCenterWorkbenchState(
    private val remoteService: ConfigCenterRemoteService,
) {
    var namespace by mutableStateOf("kcloud")
    var active by mutableStateOf("dev")
    var key by mutableStateOf("")
    var value by mutableStateOf("")
    var updateTimeMillis by mutableStateOf<Long?>(null)
        private set

    var statusMessage by mutableStateOf("输入 namespace、active、key 后可直接读取或写入。")
        private set
    var isBusy by mutableStateOf(false)
        private set

    suspend fun readValue() {
        require(namespace.isNotBlank()) { "namespace 不能为空" }
        require(key.isNotBlank()) { "key 不能为空" }
        runBusy(
            successMessage = "已读取配置",
        ) {
            val loaded = remoteService.readValue(
                namespace = namespace,
                key = key,
                active = active,
            )
            value = loaded.value.orEmpty()
            updateTimeMillis = loaded.updateTimeMillis
            if (loaded.value == null) {
                statusMessage = "未找到对应配置，可直接写入新值。"
            }
        }
    }

    suspend fun writeValue() {
        require(namespace.isNotBlank()) { "namespace 不能为空" }
        require(key.isNotBlank()) { "key 不能为空" }
        runBusy(
            successMessage = "已写入配置",
        ) {
            val saved = remoteService.writeValue(
                ConfigCenterValueWriteRequest(
                    namespace = namespace,
                    active = active,
                    key = key,
                    value = value,
                ),
            )
            value = saved.value.orEmpty()
            updateTimeMillis = saved.updateTimeMillis
        }
    }

    private suspend fun runBusy(
        successMessage: String,
        block: suspend () -> Unit,
    ) {
        isBusy = true
        runCatching {
            block()
        }.onSuccess {
            if (statusMessage == "未找到对应配置，可直接写入新值。") {
                return@onSuccess
            }
            statusMessage = successMessage
        }.onFailure { throwable ->
            statusMessage = throwable.message ?: "操作失败"
        }
        isBusy = false
    }
}
