package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "enum_entry_meta")
interface EnumEntryMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "declaration_id")
    val declaration: DeclarationMeta

    @IdView
    val declarationId: String

    val name: String

    @Column(name = "arguments_json")
    val argumentsJson: String?

    @Column(name = "body_text")
    val bodyText: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
