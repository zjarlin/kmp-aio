package com.kcloud.plugins.ai.ollama

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.executor.ollama.client.nameWithoutTag
import ai.koog.prompt.executor.ollama.client.toLLModel
import ai.koog.prompt.message.Message
import com.kcloud.model.AiProviderIds
import com.kcloud.model.AiSettings
import com.kcloud.plugins.ai.spi.AiConnectionTestResult
import com.kcloud.plugins.ai.spi.AiModelOption
import com.kcloud.plugins.ai.spi.AiProvider
import com.kcloud.plugins.ai.spi.AiProviderDescriptor
import com.kcloud.plugins.ai.spi.AiProviderType
import org.koin.core.annotation.Single

@Single
class OllamaAiProvider : AiProvider {
    override val descriptor = AiProviderDescriptor(
        providerId = AiProviderIds.OLLAMA,
        displayName = "Ollama",
        providerType = AiProviderType.LOCAL,
        supportsModelDiscovery = true
    )

    override suspend fun testConnection(settings: AiSettings): AiConnectionTestResult {
        val config = settings.ollama
        if (config.baseUrl.isBlank()) {
            return AiConnectionTestResult.failure("Ollama Base URL 不能为空")
        }
        if (config.model.isBlank()) {
            return AiConnectionTestResult.failure("Ollama 模型名不能为空")
        }

        return runCatching {
            val client = OllamaClient(baseUrl = config.baseUrl.trim().trimEnd('/'))
            val modelCard = client.getModelOrNull(config.model.trim(), pullIfMissing = false)
                ?: return AiConnectionTestResult.failure(
                    message = "未找到模型 ${config.model.trim()}",
                    details = "请先确认 Ollama 已经拉取该模型。"
                )

            val executor = SingleLLMPromptExecutor(client)
            val response = executor.execute(
                prompt = prompt("kcloud-ollama-healthcheck") {
                    system("You are a connection health check. Reply with exactly OK.")
                    user("Reply with OK only.")
                },
                model = modelCard.toLLModel()
            )

            val assistantMessage = response.firstOrNull { it is Message.Assistant } as? Message.Assistant
            val reply = assistantMessage?.content?.trim().orEmpty()

            AiConnectionTestResult.success(
                message = "Ollama 连接成功",
                details = buildString {
                    append("baseUrl=")
                    append(config.baseUrl.trim())
                    append("，model=")
                    append(modelCard.nameWithoutTag)
                    if (reply.isNotBlank()) {
                        append("，reply=")
                        append(reply)
                    }
                }
            )
        }.getOrElse { throwable ->
            AiConnectionTestResult.failure(
                message = "Ollama 连接失败",
                details = throwable.message ?: throwable::class.simpleName.orEmpty()
            )
        }
    }

    override suspend fun discoverModels(settings: AiSettings): List<AiModelOption> {
        val baseUrl = settings.ollama.baseUrl.trim()
        if (baseUrl.isBlank()) {
            return emptyList()
        }

        return runCatching {
            OllamaClient(baseUrl = baseUrl.trimEnd('/'))
                .getModels()
                .map { model ->
                    AiModelOption(
                        id = model.name,
                        displayName = model.nameWithoutTag
                    )
                }
        }.getOrDefault(emptyList())
    }
}
