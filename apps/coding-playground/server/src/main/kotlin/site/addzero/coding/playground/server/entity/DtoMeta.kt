package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "dto_meta")
interface DtoMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "context_id")
    val context: BoundedContextMeta

    @IdView
    val contextId: String

    @ManyToOne
    @JoinColumn(name = "entity_id")
    val entity: EntityMeta?

    @IdView
    val entityId: String?

    val name: String
    val code: String
    val kind: String
    val description: String?

    @Column(name = "tags_json")
    val tagsJson: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "dto")
    val fields: List<DtoFieldMeta>
}
