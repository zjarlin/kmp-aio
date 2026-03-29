package site.addzero.kcloud.plugins.system.aichat.model

import org.babyfish.jimmer.sql.*
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "ai_chat_message")
interface AiChatMessage : BaseEntity {
    @Key
    @Column(name = "message_key")
    val messageKey: String

    @ManyToOne
    @JoinColumn(name = "session_id")
    val session: AiChatSession

    val role: String

    val content: String
}
