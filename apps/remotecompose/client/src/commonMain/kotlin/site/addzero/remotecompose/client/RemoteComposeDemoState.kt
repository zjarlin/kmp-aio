package site.addzero.remotecompose.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.encodeToString
import site.addzero.remotecompose.shared.RemoteComposeAction
import site.addzero.remotecompose.shared.RemoteComposeJson
import site.addzero.remotecompose.shared.RemoteComposeLocale
import site.addzero.remotecompose.shared.RemoteComposeScreenPayload
import site.addzero.remotecompose.shared.RemoteComposeScreenSummary
import site.addzero.remotecompose.shared.RemoteComposeTone

class RemoteComposeDemoState(
    private val config: RemoteComposeClientConfig,
    private val service: RemoteComposeDemoService,
    private val localePreferences: RemoteComposeLocalePreferences,
) {
    private var hasBootstrapped = false

    var locale by mutableStateOf(localePreferences.read())
        private set

    var screens by mutableStateOf<List<RemoteComposeScreenSummary>>(emptyList())
        private set

    var selectedScreenId by mutableStateOf<String?>(null)
        private set

    var currentScreen by mutableStateOf<RemoteComposeScreenPayload?>(null)
        private set

    var inspectorJson by mutableStateOf("")
        private set

    var statusMessage by mutableStateOf(
        if (locale == RemoteComposeLocale.ZH_CN) {
            "等待拉取服务端 schema。"
        } else {
            "Waiting for the server schema."
        }
    )
        private set

    var statusTone by mutableStateOf(RemoteComposeTone.INFO)
        private set

    var isLoading by mutableStateOf(false)
        private set

    val baseUrl: String
        get() = config.baseUrl

    suspend fun ensureLoaded() {
        if (hasBootstrapped) {
            return
        }
        hasBootstrapped = true
        refresh()
    }

    suspend fun refresh() {
        isLoading = true
        try {
            val loadedScreens = service.fetchScreens(locale)
            screens = loadedScreens
            val targetScreenId = selectedScreenId
                ?.takeIf { current -> loadedScreens.any { it.id == current } }
                ?: loadedScreens.firstOrNull()?.id

            if (targetScreenId == null) {
                currentScreen = null
                inspectorJson = ""
                updateStatus(
                    message = localize(
                        zh = "服务端目前没有可渲染 screen。",
                        en = "The server has no renderable screens right now.",
                    ),
                    tone = RemoteComposeTone.WARNING,
                )
            } else {
                loadScreen(targetScreenId)
            }
        } catch (error: Throwable) {
            currentScreen = null
            inspectorJson = ""
            updateStatus(
                message = localize(
                    zh = "拉取 schema 失败：${error.message ?: error::class.simpleName.orEmpty()}",
                    en = "Failed to load schema: ${error.message ?: error::class.simpleName.orEmpty()}",
                ),
                tone = RemoteComposeTone.DANGER,
            )
        } finally {
            isLoading = false
        }
    }

    suspend fun selectScreen(screenId: String) {
        if (screenId == selectedScreenId && currentScreen != null) {
            return
        }

        isLoading = true
        try {
            loadScreen(screenId)
        } catch (error: Throwable) {
            updateStatus(
                message = localize(
                    zh = "切换 screen 失败：${error.message ?: error::class.simpleName.orEmpty()}",
                    en = "Failed to switch screen: ${error.message ?: error::class.simpleName.orEmpty()}",
                ),
                tone = RemoteComposeTone.DANGER,
            )
        } finally {
            isLoading = false
        }
    }

    suspend fun toggleLocale() {
        locale = locale.toggle()
        localePreferences.write(locale)
        updateStatus(
            message = localize(
                zh = "语言已切换，正在重新拉取 schema。",
                en = "Language changed. Reloading schema.",
            ),
            tone = RemoteComposeTone.INFO,
        )
        refresh()
    }

    suspend fun handleAction(action: RemoteComposeAction) {
        when (action) {
            is RemoteComposeAction.OpenScreen -> selectScreen(action.screenId)
            is RemoteComposeAction.Refresh -> refresh()
            is RemoteComposeAction.ShowMessage -> updateStatus(
                message = action.message,
                tone = action.tone,
            )
        }
    }

    private suspend fun loadScreen(screenId: String) {
        val payload = service.fetchScreen(screenId, locale)
        selectedScreenId = screenId
        currentScreen = payload
        inspectorJson = RemoteComposeJson.instance.encodeToString(payload)
        updateStatus(
            message = payload.serverNote,
            tone = RemoteComposeTone.INFO,
        )
    }

    private fun updateStatus(
        message: String,
        tone: RemoteComposeTone,
    ) {
        statusMessage = message
        statusTone = tone
    }

    private fun localize(
        zh: String,
        en: String,
    ): String {
        return if (locale == RemoteComposeLocale.ZH_CN) zh else en
    }
}
