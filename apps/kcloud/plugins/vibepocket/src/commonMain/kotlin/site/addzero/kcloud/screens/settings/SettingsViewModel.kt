package site.addzero.vibepocket.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.model.ConfigRuntimeInfo
import site.addzero.vibepocket.music.SunoWorkflowService
import site.addzero.vibepocket.platform.DirectoryLauncher

@KoinViewModel
class SettingsViewModel : ViewModel() {
    private val screenScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var sunoToken by mutableStateOf("")
        private set

    var sunoBaseUrl by mutableStateOf(site.addzero.vibepocket.api.suno.SunoApiClient.DEFAULT_BASE_URL)
        private set

    var sunoCallbackUrl by mutableStateOf("")
        private set

    var loaded by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var runtimeInfo by mutableStateOf<ConfigRuntimeInfo?>(null)
        private set

    var feedbackMessage by mutableStateOf<String?>(null)
        private set

    var feedbackIsError by mutableStateOf(false)
        private set

    init {
        reloadFromServer()
    }

    fun updateSunoToken(
        value: String,
    ) {
        sunoToken = value
    }

    fun updateSunoBaseUrl(
        value: String,
    ) {
        sunoBaseUrl = value
    }

    fun updateSunoCallbackUrl(
        value: String,
    ) {
        sunoCallbackUrl = value
    }

    fun reloadFromServer() {
        screenScope.launch {
            reloadFromServerInternal()
        }
    }

    fun reloadWithFeedback() {
        screenScope.launch {
            reloadFromServerInternal()
            feedbackIsError = false
            feedbackMessage = "已重新读取当前本地配置。"
        }
    }

    fun saveConfig() {
        screenScope.launch {
            isSaving = true
            try {
                SunoWorkflowService.saveConfig(
                    apiToken = sunoToken,
                    baseUrl = sunoBaseUrl,
                    callbackUrl = sunoCallbackUrl,
                )
                reloadFromServerInternal()
                feedbackIsError = false
                feedbackMessage = when (runtimeInfo?.storage) {
                    "sqlite" -> "已保存到本地 SQLite。"
                    else -> "配置已保存。"
                }
            } catch (error: Throwable) {
                feedbackIsError = true
                feedbackMessage = error.message?.takeIf { it.isNotBlank() } ?: "保存配置失败"
            } finally {
                isSaving = false
            }
        }
    }

    fun openCacheDir() {
        val cacheDir = runtimeInfo?.cacheDir
        if (cacheDir.isNullOrBlank()) {
            feedbackIsError = true
            feedbackMessage = "当前没有可打开的缓存目录。"
            return
        }

        val opened = DirectoryLauncher.openDirectory(cacheDir)
        feedbackIsError = !opened
        feedbackMessage = if (opened) {
            "已打开缓存目录。"
        } else {
            "打开缓存目录失败：$cacheDir"
        }
    }

    private suspend fun reloadFromServerInternal() {
        val runtimeConfig = SunoWorkflowService.loadConfig()
        sunoToken = runtimeConfig.apiToken
        sunoBaseUrl = runtimeConfig.baseUrl
        sunoCallbackUrl = runtimeConfig.callbackUrl
        runtimeInfo = runCatching { ServerApiClient.configApi.getRuntimeInfo() }.getOrNull()
        loaded = true
    }

    override fun onCleared() {
        screenScope.cancel()
        super.onCleared()
    }
}
