package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "generation_target_meta")
interface GenerationTargetMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "project_id")
    val project: ProjectMeta

    @IdView
    val projectId: String

    @ManyToOne
    @JoinColumn(name = "context_id")
    val context: BoundedContextMeta

    @IdView
    val contextId: String

    val name: String
    val key: String
    val description: String?

    @Column(name = "output_root")
    val outputRoot: String

    @Column(name = "package_name")
    val packageName: String

    @Column(name = "scaffold_preset")
    val scaffoldPreset: String

    @Column(name = "variables_json")
    val variablesJson: String?

    @Column(name = "enable_etl")
    val enableEtl: Boolean

    @Column(name = "auto_integrate_composite_build")
    val autoIntegrateCompositeBuild: Boolean

    @Column(name = "managed_marker")
    val managedMarker: String

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @ManyToMany
    @JoinTable(
        name = "generation_target_template",
        joinColumnName = "generation_target_id",
        inverseJoinColumnName = "template_id",
    )
    val templates: List<TemplateMeta>
}
