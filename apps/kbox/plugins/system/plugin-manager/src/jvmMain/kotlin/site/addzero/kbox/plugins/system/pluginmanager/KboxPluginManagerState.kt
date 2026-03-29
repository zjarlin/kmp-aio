package site.addzero.kbox.plugins.system.pluginmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kbox.plugin.api.KboxInstalledPluginSnapshot
import site.addzero.kbox.plugin.api.KboxPluginManagerService

@Single
class KboxPluginManagerState(
    private val pluginManagerService: KboxPluginManagerService,
) {
    val plugins = mutableStateListOf<KboxInstalledPluginSnapshot>()

    var selectedPluginId by mutableStateOf("")
        private set
    var installSourceDir by mutableStateOf("")
    var statusText by mutableStateOf("等待扫描插件目录")
        private set
    var statusIsError by mutableStateOf(false)
        private set
    var isBusy by mutableStateOf(false)
        private set

    val selectedPlugin: KboxInstalledPluginSnapshot?
        get() = plugins.firstOrNull { plugin -> plugin.pluginId == selectedPluginId }

    suspend fun refresh() {
        runAction("插件列表已刷新") {
            pluginManagerService.refresh()
            plugins.replaceAll(pluginManagerService.installedPlugins.value)
            if (selectedPluginId.isBlank()) {
                selectedPluginId = plugins.firstOrNull()?.pluginId.orEmpty()
            }
        }
    }

    suspend fun installFromDirectory() {
        runAction("插件安装完成") {
            val result = pluginManagerService.installFromDirectory(installSourceDir)
            refresh()
            selectedPluginId = result.pluginId
            statusText = result.message
            statusIsError = !result.success
        }
    }

    suspend fun enableSelected() {
        val pluginId = requireSelectedPluginId()
        runAction("插件已启用") {
            val result = pluginManagerService.enable(pluginId)
            refresh()
            statusText = result.message
            statusIsError = !result.success
        }
    }

    suspend fun disableSelected() {
        val pluginId = requireSelectedPluginId()
        runAction("插件已停用") {
            val result = pluginManagerService.disable(pluginId)
            refresh()
            statusText = result.message
            statusIsError = !result.success
        }
    }

    suspend fun uninstallSelected() {
        val pluginId = requireSelectedPluginId()
        runAction("插件已卸载") {
            val result = pluginManagerService.uninstall(pluginId)
            refresh()
            selectedPluginId = plugins.firstOrNull()?.pluginId.orEmpty()
            statusText = result.message
            statusIsError = !result.success
        }
    }

    fun selectPlugin(
        pluginId: String,
    ) {
        selectedPluginId = pluginId
    }

    private fun requireSelectedPluginId(): String {
        return selectedPlugin?.pluginId
            ?: throw IllegalStateException("请先选择一个插件")
    }

    private suspend fun runAction(
        defaultSuccessText: String,
        block: suspend () -> Unit,
    ) {
        isBusy = true
        statusIsError = false
        try {
            block()
            if (statusText.isBlank()) {
                statusText = defaultSuccessText
            }
        } catch (error: Throwable) {
            statusIsError = true
            statusText = error.message ?: "执行失败"
        } finally {
            isBusy = false
        }
    }

    private fun <T> MutableList<T>.replaceAll(
        values: List<T>,
    ) {
        clear()
        addAll(values)
    }
}
