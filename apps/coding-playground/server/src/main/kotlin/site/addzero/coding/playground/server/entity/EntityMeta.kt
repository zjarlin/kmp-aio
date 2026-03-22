package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "entity_meta")
interface EntityMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "context_id")
    val context: BoundedContextMeta

    @IdView
    val contextId: String

    val name: String
    val code: String

    @Column(name = "table_name")
    val tableName: String

    val description: String?

    @Column(name = "aggregate_root")
    val aggregateRoot: Boolean

    @Column(name = "tags_json")
    val tagsJson: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "entity")
    val fields: List<FieldMeta>

    @OneToMany(mappedBy = "sourceEntity")
    val sourceRelations: List<RelationMeta>

    @OneToMany(mappedBy = "targetEntity")
    val targetRelations: List<RelationMeta>

    @OneToMany(mappedBy = "entity")
    val dtos: List<DtoMeta>
}
