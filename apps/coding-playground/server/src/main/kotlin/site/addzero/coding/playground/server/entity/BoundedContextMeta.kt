package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "bounded_context_meta")
interface BoundedContextMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "project_id")
    val project: ProjectMeta

    @IdView
    val projectId: String

    val name: String
    val code: String
    val description: String?

    @Column(name = "tags_json")
    val tagsJson: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "context")
    val entities: List<EntityMeta>

    @OneToMany(mappedBy = "context")
    val relations: List<RelationMeta>

    @OneToMany(mappedBy = "context")
    val dtos: List<DtoMeta>

    @OneToMany(mappedBy = "context")
    val templates: List<TemplateMeta>

    @OneToMany(mappedBy = "context")
    val generationTargets: List<GenerationTargetMeta>
}
