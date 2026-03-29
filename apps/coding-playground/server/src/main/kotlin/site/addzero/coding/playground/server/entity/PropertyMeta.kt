package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "property_meta")
interface PropertyMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "declaration_id")
    val declaration: DeclarationMeta

    @IdView
    val declarationId: String

    val name: String
    val type: String
    val mutable: Boolean
    val nullable: Boolean
    val visibility: String

    @Column(name = "initializer")
    val initializer: String?

    @Column(name = "is_override")
    val isOverride: Boolean

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
