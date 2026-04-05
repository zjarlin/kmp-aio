package site.addzero.kcloud.plugins.system.configcenter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterKeyDefinition
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_ANTHROPIC
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_DEEPSEEK
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_GEMINI
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OLLAMA
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OPENAI
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OPENROUTER
import site.addzero.kcloud.plugins.system.aichat.config.AiChatConfigKeys
import site.addzero.kcloud.plugins.system.configcenter.api.Apis
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueWriteRequest

data class AiConfigProviderPreset(
    val label: String,
    val vendor: String,
    val apiUrlDefinition: ConfigCenterKeyDefinition,
    val modelDefinition: ConfigCenterKeyDefinition,
)

private val AI_PROVIDER_PRESETS = listOf(
    AiConfigProviderPreset(
        label = "OpenAI",
        vendor = AI_CHAT_VENDOR_OPENAI,
        apiUrlDefinition = AiChatConfigKeys.presetOpenaiApiUrl,
        modelDefinition = AiChatConfigKeys.presetOpenaiModel,
    ),
    AiConfigProviderPreset(
        label = "Anthropic",
        vendor = AI_CHAT_VENDOR_ANTHROPIC,
        apiUrlDefinition = AiChatConfigKeys.presetAnthropicApiUrl,
        modelDefinition = AiChatConfigKeys.presetAnthropicModel,
    ),
    AiConfigProviderPreset(
        label = "DeepSeek",
        vendor = AI_CHAT_VENDOR_DEEPSEEK,
        apiUrlDefinition = AiChatConfigKeys.presetDeepseekApiUrl,
        modelDefinition = AiChatConfigKeys.presetDeepseekModel,
    ),
    AiConfigProviderPreset(
        label = "OpenRouter",
        vendor = AI_CHAT_VENDOR_OPENROUTER,
        apiUrlDefinition = AiChatConfigKeys.presetOpenrouterApiUrl,
        modelDefinition = AiChatConfigKeys.presetOpenrouterModel,
    ),
    AiConfigProviderPreset(
        label = "Gemini",
        vendor = AI_CHAT_VENDOR_GEMINI,
        apiUrlDefinition = AiChatConfigKeys.presetGeminiApiUrl,
        modelDefinition = AiChatConfigKeys.presetGeminiModel,
    ),
    AiConfigProviderPreset(
        label = "Ollama",
        vendor = AI_CHAT_VENDOR_OLLAMA,
        apiUrlDefinition = AiChatConfigKeys.presetOllamaApiUrl,
        modelDefinition = AiChatConfigKeys.presetOllamaModel,
    ),
)

private val AI_EDITABLE_DEFINITIONS = listOf(
    AiChatConfigKeys.transport,
    AiChatConfigKeys.vendor,
    AiChatConfigKeys.apiUrl,
    AiChatConfigKeys.apiKey,
    AiChatConfigKeys.model,
    AiChatConfigKeys.systemPrompt,
)

@Single
class ConfigCenterWorkbenchState {
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

    val aiProviderPresets: List<AiConfigProviderPreset>
        get() = AI_PROVIDER_PRESETS

    val aiKeyPresets: List<ConfigCenterKeyDefinition>
        get() = AI_EDITABLE_DEFINITIONS

    fun useAiNamespace() {
        namespace = AiChatConfigKeys.NAMESPACE
    }

    fun applyAiKeyPreset(
        definition: ConfigCenterKeyDefinition,
    ) {
        useAiNamespace()
        key = definition.key
        if (value.isBlank()) {
            value = definition.defaultValue.orEmpty()
        }
    }

    fun applyAiProviderPreset(
        preset: AiConfigProviderPreset,
    ) {
        useAiNamespace()
        key = AiChatConfigKeys.VENDOR
        value = preset.vendor
    }

    fun applyAiUrlPreset(
        preset: AiConfigProviderPreset,
    ) {
        useAiNamespace()
        key = AiChatConfigKeys.API_URL
        value = preset.apiUrlDefinition.defaultValue
            ?: error("缺少 ${preset.apiUrlDefinition.key} 的默认值定义。")
    }

    fun applyAiModelPreset(
        preset: AiConfigProviderPreset,
    ) {
        useAiNamespace()
        key = AiChatConfigKeys.MODEL
        value = preset.modelDefinition.defaultValue
            ?: error("缺少 ${preset.modelDefinition.key} 的默认值定义。")
    }

    suspend fun readValue() {
        require(namespace.isNotBlank()) { "namespace 不能为空" }
        require(key.isNotBlank()) { "key 不能为空" }
        runBusy(
            successMessage = "已读取配置",
        ) {
            val loaded = Apis.configCenterApi.getConfigCenterValue(
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
            val saved = Apis.configCenterApi.putConfigCenterValue(
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
