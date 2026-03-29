package site.addzero.kcloud.plugins.system.aichat.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
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
