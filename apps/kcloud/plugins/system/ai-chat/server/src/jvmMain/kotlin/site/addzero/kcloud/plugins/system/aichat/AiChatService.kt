package site.addzero.kcloud.plugins.system.aichat

import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterBeanFactory
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.ConfigCenterKeyDefinition
import site.addzero.kcloud.plugins.system.aichat.model.AiChatMessage
import site.addzero.kcloud.plugins.system.aichat.model.AiChatSession
import site.addzero.kcloud.plugins.system.aichat.model.by
import site.addzero.kcloud.plugins.system.aichat.api.AiChatConversationDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatDeleteResult
import site.addzero.kcloud.plugins.system.aichat.api.AiChatMessageDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatProviderConfigDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionDto
import site.addzero.kcloud.plugins.system.aichat.config.AiChatConfigKeys
import site.addzero.kcloud.plugins.system.aichat.provider.AiChatCompletionGateway
import site.addzero.kcloud.plugins.system.aichat.provider.AiChatCompletionRequest
import site.addzero.kcloud.plugins.system.aichat.provider.AiChatTurn
import site.addzero.kcloud.plugins.system.configcenter.spi.RuntimeConfigCenterActive
import java.util.*

private const val DEFAULT_SESSION_TITLE = "新会话"

@Single
class AiChatService(
    private val sqlClient: KSqlClient,
    private val completionGateway: AiChatCompletionGateway,
    private val configCenterBeanFactory: ConfigCenterBeanFactory,
    private val runtimeConfigCenterActive: RuntimeConfigCenterActive,
) {
    fun listSessions(): List<AiChatSessionDto> {
        return allSessions()
            .sortedByDescending { it.updateTime ?: it.createTime }
            .map { it.toDto() }
    }

    fun createSession(
        title: String,
    ): AiChatSessionDto {
        val session = AiChatSession {
            sessionKey = UUID.randomUUID().toString()
            this.title = title.trim().ifBlank { DEFAULT_SESSION_TITLE }
            archived = false
        }
        val saveResult = sqlClient.save(session)
        val saved = saveResult.modifiedEntity
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
        val userMessage = AiChatMessage {
            messageKey = UUID.randomUUID().toString()
            this.session = sessionRef(session.id)
            role = "user"
            this.content = normalizedContent
        }
        sqlClient.save(userMessage)
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
        val assistantMessage = AiChatMessage {
            messageKey = UUID.randomUUID().toString()
            this.session = sessionRef(session.id)
            role = "assistant"
            this.content = assistantReply
        }
        sqlClient.save(assistantMessage)

        val updatedSession = AiChatSession {
            id = session.id
            sessionKey = session.sessionKey
            title = resolveSessionTitle(
                existingTitle = session.title,
                latestMessage = normalizedContent,
            )
            archived = session.archived
            createTime = session.createTime
        }
        val sessionSaveResult = sqlClient.save(updatedSession)
        val savedSession = sessionSaveResult.modifiedEntity

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
        return AiChatSession {
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
        val env = runtimeEnv()
        return AiChatProviderConfigDto(
            transport = requireConfigValue(env, AiChatConfigKeys.transport, "apiTransport"),
            vendor = requireConfigValue(env, AiChatConfigKeys.vendor, "provider", "apiVendor", "apiProvider"),
            baseUrl = requireConfigValue(env, AiChatConfigKeys.apiUrl, "baseUrl"),
            apiKey = readFirstConfigValue(env, AiChatConfigKeys.apiKey, "token"),
            model = requireConfigValue(env, AiChatConfigKeys.model),
            systemPrompt = requireConfigValue(env, AiChatConfigKeys.systemPrompt),
        )
    }

    private fun readFirstConfigValue(
        env: ConfigCenterEnv,
        definition: ConfigCenterKeyDefinition,
        vararg aliases: String,
    ): String {
        return buildList {
            add(definition.key)
            addAll(aliases)
        }.firstNotNullOfOrNull { key ->
            env.string(key)?.trim()?.takeIf(String::isNotBlank)
        } ?: definition.defaultValue.orEmpty()
    }

    private fun requireConfigValue(
        env: ConfigCenterEnv,
        definition: ConfigCenterKeyDefinition,
        vararg aliases: String,
    ): String {
        return readFirstConfigValue(env, definition, *aliases)
            .ifBlank {
                error(
                    "配置中心缺少必填项 namespace=${AiChatConfigKeys.NAMESPACE} " +
                        "active=${runtimeConfigCenterActive.value} key=${definition.key}",
                )
            }
    }

    private fun runtimeEnv(): ConfigCenterEnv {
        return configCenterBeanFactory.env(
            namespace = AiChatConfigKeys.NAMESPACE,
            active = runtimeConfigCenterActive.value,
        )
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
