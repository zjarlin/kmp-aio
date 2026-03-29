package site.addzero.kcloud.plugins.system.aichat.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "ai_chat_session")
interface AiChatSession : BaseEntity {
    @Key
    @Column(name = "session_key")
    val sessionKey: String

    val title: String

    val archived: Boolean

    @OneToMany(mappedBy = "session")
    val messages: List<AiChatMessage>
}
