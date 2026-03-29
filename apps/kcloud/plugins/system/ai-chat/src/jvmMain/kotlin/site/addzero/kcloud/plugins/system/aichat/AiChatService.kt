package site.addzero.kcloud.plugins.system.aichat

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.aichat.model.AiChatMessage
import site.addzero.kcloud.plugins.system.aichat.model.AiChatSession
import site.addzero.kcloud.plugins.system.aichat.model.by
import site.addzero.kcloud.system.api.AiChatConversationDto
import site.addzero.kcloud.system.api.AiChatDeleteResult
import site.addzero.kcloud.system.api.AiChatMessageDto
import site.addzero.kcloud.system.api.AiChatSessionDto
import java.util.UUID

private const val DEFAULT_SESSION_TITLE = "新会话"
private const val AI_PROVIDER_PLACEHOLDER =
    "模型提供方尚未接通。请先在配置中心的 kcloud.ai 命名空间下补齐 provider 配置。"

@Single
class AiChatService(
    private val sqlClient: KSqlClient,
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

    fun sendMessage(
        sessionId: Long,
        content: String,
    ): AiChatConversationDto {
        val normalizedContent = content.trim()
        require(normalizedContent.isNotBlank()) { "消息不能为空" }

        val session = sessionOrThrow(sessionId)
        sqlClient.save(
            new(AiChatMessage::class).by {
                messageKey = UUID.randomUUID().toString()
                this.session = sessionRef(session.id)
                role = "user"
                this.content = normalizedContent
            },
        )
        sqlClient.save(
            new(AiChatMessage::class).by {
                messageKey = UUID.randomUUID().toString()
                this.session = sessionRef(session.id)
                role = "assistant"
                this.content = AI_PROVIDER_PLACEHOLDER
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
