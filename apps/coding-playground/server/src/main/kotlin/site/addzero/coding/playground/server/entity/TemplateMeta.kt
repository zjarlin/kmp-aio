package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "template_meta")
interface TemplateMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "context_id")
    val context: BoundedContextMeta

    @IdView
    val contextId: String

    @ManyToOne
    @JoinColumn(name = "etl_wrapper_id")
    val etlWrapper: EtlWrapperMeta?

    @IdView
    val etlWrapperId: String?

    val name: String
    val key: String
    val description: String?

    @Column(name = "output_kind")
    val outputKind: String

    val body: String

    @Column(name = "relative_output_path")
    val relativeOutputPath: String

    @Column(name = "file_name_template")
    val fileNameTemplate: String

    @Column(name = "tags_json")
    val tagsJson: String?

    @Column(name = "order_index")
    val orderIndex: Int

    val enabled: Boolean

    @Column(name = "managed_by_generator")
    val managedByGenerator: Boolean

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @ManyToMany(mappedBy = "templates")
    val generationTargets: List<GenerationTargetMeta>
}
