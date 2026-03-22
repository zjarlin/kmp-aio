package site.addzero.vibepocket.model

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

/**
 * Persona 记录表
 */
@Entity
@Table(name = "persona_record")
interface PersonaRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    /** Persona ID（唯一标识） */
    @Key
    @Column(name = "persona_id")
    val personaId: String

    /** 名称 */
    val name: String

    /** 描述 */
    val description: String

    /** 创建时间 */
    @Column(name = "created_at")
    val createdAt: LocalDateTime
}
