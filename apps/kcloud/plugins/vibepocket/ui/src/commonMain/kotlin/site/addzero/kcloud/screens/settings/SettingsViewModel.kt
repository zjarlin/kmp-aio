package site.addzero.kcloud.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.api.ServerApiClient
import site.addzero.kcloud.vibepocket.model.ConfigRuntimeInfo
import site.addzero.kcloud.music.SunoWorkflowService
import site.addzero.kcloud.music.SunoRuntimeConfig
import site.addzero.kcloud.platform.DirectoryLauncher

data class SettingsFeedbackState(
    val message: String? = null,
    val isError: Boolean = false,
)

data class SettingsScreenState(
    val runtimeConfig: SunoRuntimeConfig = SunoRuntimeConfig(),
    val loaded: Boolean = false,
    val isSaving: Boolean = false,
    val runtimeInfo: ConfigRuntimeInfo? = null,
    val feedback: SettingsFeedbackState = SettingsFeedbackState(),
)

@KoinViewModel
class SettingsViewModel : ViewModel() {
    private val screenScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var state by mutableStateOf(SettingsScreenState())
        private set

    init {
        reloadFromServer()
    }

    fun updateSunoToken(
        value: String,
    ) {
        state = state.copy(
            runtimeConfig = state.runtimeConfig.copy(apiToken = value),
        )
    }

    fun updateSunoBaseUrl(
        value: String,
    ) {
        state = state.copy(
            runtimeConfig = state.runtimeConfig.copy(baseUrl = value),
        )
    }

    fun updateSunoCallbackUrl(
        value: String,
    ) {
        state = state.copy(
            runtimeConfig = state.runtimeConfig.copy(callbackUrl = value),
        )
    }

    fun reloadFromServer() {
        screenScope.launch {
            reloadFromServerInternal()
        }
    }

    fun reloadWithFeedback() {
        screenScope.launch {
            reloadFromServerInternal()
            state = state.copy(
                feedback = SettingsFeedbackState(
                    message = "已重新读取当前本地配置。",
                    isError = false,
                ),
            )
        }
    }

    fun saveConfig() {
        screenScope.launch {
            state = state.copy(isSaving = true)
            try {
                val runtimeConfig = state.runtimeConfig
                SunoWorkflowService.saveConfig(
                    apiToken = runtimeConfig.apiToken,
                    baseUrl = runtimeConfig.baseUrl,
                    callbackUrl = runtimeConfig.callbackUrl,
                )
                reloadFromServerInternal()
                state = state.copy(
                    feedback = SettingsFeedbackState(
                        message = when (state.runtimeInfo?.storage) {
                            "sqlite" -> "已保存到本地 SQLite。"
                            else -> "配置已保存。"
                        },
                        isError = false,
                    ),
                )
            } catch (error: Throwable) {
                state = state.copy(
                    feedback = SettingsFeedbackState(
                        message = error.message?.takeIf { it.isNotBlank() } ?: "保存配置失败",
                        isError = true,
                    ),
                )
            } finally {
                state = state.copy(isSaving = false)
            }
        }
    }

    fun openCacheDir() {
        val cacheDir = state.runtimeInfo?.cacheDir
        if (cacheDir.isNullOrBlank()) {
            state = state.copy(
                feedback = SettingsFeedbackState(
                    message = "当前没有可打开的缓存目录。",
                    isError = true,
                ),
            )
            return
        }

        val opened = DirectoryLauncher.openDirectory(cacheDir)
        state = state.copy(
            feedback = SettingsFeedbackState(
                message = if (opened) {
                    "已打开缓存目录。"
                } else {
                    "打开缓存目录失败：$cacheDir"
                },
                isError = !opened,
            ),
        )
    }

    private suspend fun reloadFromServerInternal() {
        val runtimeConfig = SunoWorkflowService.loadConfig()
        state = state.copy(
            runtimeConfig = runtimeConfig,
            runtimeInfo = runCatching { ServerApiClient.configApi.getRuntimeInfo() }.getOrNull(),
            loaded = true,
        )
    }

    override fun onCleared() {
        super.onCleared()
        screenScope.cancel()
    }
}
