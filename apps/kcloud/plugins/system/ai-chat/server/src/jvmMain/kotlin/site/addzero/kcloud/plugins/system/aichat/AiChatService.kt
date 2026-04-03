package site.addzero.kcloud.plugins.system.aichat

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_DEFAULT_SYSTEM_PROMPT
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_TRANSPORT_HTTP
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OPENAI
import site.addzero.kcloud.plugins.system.aichat.model.AiChatMessage
import site.addzero.kcloud.plugins.system.aichat.model.AiChatSession
import site.addzero.kcloud.plugins.system.aichat.model.by
import site.addzero.kcloud.plugins.system.aichat.api.AiChatConversationDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatDeleteResult
import site.addzero.kcloud.plugins.system.aichat.api.AiChatMessageDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatProviderConfigDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionDto
import site.addzero.kcloud.plugins.system.aichat.provider.AiChatCompletionGateway
import site.addzero.kcloud.plugins.system.aichat.provider.AiChatCompletionRequest
import site.addzero.kcloud.plugins.system.aichat.provider.AiChatTurn
import site.addzero.kcloud.plugins.system.configcenter.spi.ConfigValueServiceSpi
import java.util.*

private const val DEFAULT_SESSION_TITLE = "新会话"

@Single
class AiChatService(
    private val sqlClient: KSqlClient,
    private val completionGateway: AiChatCompletionGateway,
) {
    fun listSessions(): List<AiChatSessionDto> {
        return allSessions()
            .sortedByDescending { it.updateTime ?: it.createTime }
            .map { it.toDto() }
    }

    fun createSession(
        title: String,
    ): AiChatSessionDto {
        val saved = sqlClient.save(
            new(AiChatSession::class).by {
                sessionKey = UUID.randomUUID().toString()
                this.title = title.trim().ifBlank { DEFAULT_SESSION_TITLE }
                archived = false
            },
        ).modifiedEntity
        return saved.toDto()
    }

    fun deleteSession(
        sessionId: Long,
    ): AiChatDeleteResult {
        sessionOrThrow(sessionId)
        listMessageEntities(sessionId).forEach { message ->
            sqlClient.deleteById(AiChatMessage::class, message.id)
        }
        sqlClient.deleteById(AiChatSession::class, sessionId)
        return AiChatDeleteResult(ok = true)
    }

    fun listMessages(
        sessionId: Long,
    ): List<AiChatMessageDto> {
        sessionOrThrow(sessionId)
        return listMessageEntities(sessionId)
            .sortedBy { it.createTime }
            .map { it.toDto() }
    }

    suspend fun sendMessage(
        sessionId: Long,
        content: String,
        provider: AiChatProviderConfigDto,
    ): AiChatConversationDto {
        val normalizedContent = content.trim()
        require(normalizedContent.isNotBlank()) { "消息不能为空" }

        val session = sessionOrThrow(sessionId)
        val normalizedProvider = mergeProviderConfig(provider)
        sqlClient.save(
            new(AiChatMessage::class).by {
                messageKey = UUID.randomUUID().toString()
                this.session = sessionRef(session.id)
                role = "user"
                this.content = normalizedContent
            },
        )
        val assistantReply = completionGateway.complete(
            AiChatCompletionRequest(
                transport = normalizedProvider.transport,
                vendor = normalizedProvider.vendor,
                baseUrl = normalizedProvider.baseUrl,
                apiKey = normalizedProvider.apiKey,
                model = normalizedProvider.model,
                systemPrompt = normalizedProvider.systemPrompt,
                messages = listMessageEntities(sessionId)
                    .sortedBy { it.createTime }
                    .map { message ->
                        AiChatTurn(
                            role = message.role,
                            content = message.content,
                        )
                    },
            ),
        )
        sqlClient.save(
            new(AiChatMessage::class).by {
                messageKey = UUID.randomUUID().toString()
                this.session = sessionRef(session.id)
                role = "assistant"
                this.content = assistantReply
            },
        )

        val savedSession = sqlClient.save(
            new(AiChatSession::class).by {
                id = session.id
                sessionKey = session.sessionKey
                title = resolveSessionTitle(
                    existingTitle = session.title,
                    latestMessage = normalizedContent,
                )
                archived = session.archived
                createTime = session.createTime
            },
        ).modifiedEntity

        return AiChatConversationDto(
            session = savedSession.toDto(),
            messages = listMessages(savedSession.id),
        )
    }

    private fun allSessions(): List<AiChatSession> {
        return sqlClient.createQuery(AiChatSession::class) {
            select(table)
        }.execute()
    }

    private fun sessionOrThrow(
        sessionId: Long,
    ): AiChatSession {
        return sqlClient.findById(AiChatSession::class, sessionId)
            ?: throw NoSuchElementException("会话不存在: $sessionId")
    }

    private fun sessionRef(
        sessionId: Long,
    ): AiChatSession {
        return new(AiChatSession::class).by {
            id = sessionId
        }
    }

    private fun listMessageEntities(
        sessionId: Long,
    ): List<AiChatMessage> {
        return sqlClient.createQuery(AiChatMessage::class) {
            select(table)
        }.execute().filter { message ->
            message.session.id == sessionId
        }
    }

    /**
     * 组件传来的配置优先，缺失项再回落到配置中心默认值。
     */
    private fun mergeProviderConfig(
        override: AiChatProviderConfigDto,
    ): AiChatProviderConfigDto {
        val defaults = loadDefaultProviderConfig()
        return AiChatProviderConfigDto(
            transport = override.transport.trim().ifBlank { defaults.transport },
            vendor = override.vendor.trim().ifBlank { defaults.vendor },
            baseUrl = override.baseUrl.trim().ifBlank { defaults.baseUrl },
            apiKey = override.apiKey.trim().ifBlank { defaults.apiKey },
            model = override.model.trim().ifBlank { defaults.model },
            systemPrompt = override.systemPrompt.trim().ifBlank { defaults.systemPrompt },
        )
    }

    private fun loadDefaultProviderConfig(): AiChatProviderConfigDto {
        return AiChatProviderConfigDto(
            transport = readFirstConfigValue("transport", "apiTransport").ifBlank { AI_CHAT_TRANSPORT_HTTP },
            vendor = readFirstConfigValue("vendor", "provider", "apiVendor", "apiProvider").ifBlank {
                AI_CHAT_VENDOR_OPENAI
            },
            baseUrl = readFirstConfigValue("apiUrl", "baseUrl"),
            apiKey = readFirstConfigValue("apiKey", "token"),
            model = readConfigValue("model"),
            systemPrompt = readConfigValue("systemPrompt").ifBlank { AI_CHAT_DEFAULT_SYSTEM_PROMPT },
        )
    }

    private fun readFirstConfigValue(
        vararg keys: String,
    ): String {
        return keys.firstNotNullOfOrNull { key ->
            readConfigValue(key).takeIf { it.isNotBlank() }
        }.orEmpty()
    }

    private fun readConfigValue(
        key: String,
    ): String {
        val service = runCatching {
            KoinPlatform.getKoin().get<ConfigValueServiceSpi>()
        }.getOrNull() ?: return ""
        return runCatching {
            service.readValue(
                namespace = "kcloud.ai",
                key = key,
                active = "dev",
            ).value.orEmpty()
        }.getOrDefault("")
    }
}

private fun AiChatSession.toDto(): AiChatSessionDto {
    return AiChatSessionDto(
        id = id,
        sessionKey = sessionKey,
        title = title,
        archived = archived,
        createTimeMillis = createTime.toEpochMilli(),
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}

private fun AiChatMessage.toDto(): AiChatMessageDto {
    return AiChatMessageDto(
        id = id,
        messageKey = messageKey,
        sessionId = session.id,
        role = role,
        content = content,
        createTimeMillis = createTime.toEpochMilli(),
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}

private fun resolveSessionTitle(
    existingTitle: String,
    latestMessage: String,
): String {
    if (existingTitle != DEFAULT_SESSION_TITLE) {
        return existingTitle
    }
    return latestMessage
        .lineSequence()
        .firstOrNull()
        ?.trim()
        ?.take(24)
        ?.ifBlank { DEFAULT_SESSION_TITLE }
        ?: DEFAULT_SESSION_TITLE
}
