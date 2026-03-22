package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "relation_meta")
interface RelationMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "context_id")
    val context: BoundedContextMeta

    @IdView
    val contextId: String

    @ManyToOne
    @JoinColumn(name = "source_entity_id")
    val sourceEntity: EntityMeta

    @IdView
    val sourceEntityId: String

    @ManyToOne
    @JoinColumn(name = "target_entity_id")
    val targetEntity: EntityMeta

    @IdView
    val targetEntityId: String

    val name: String
    val code: String
    val kind: String
    val nullable: Boolean
    val owner: Boolean

    @Column(name = "mapped_by")
    val mappedBy: String?

    @Column(name = "source_field_name")
    val sourceFieldName: String?

    @Column(name = "target_field_name")
    val targetFieldName: String?

    val description: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
