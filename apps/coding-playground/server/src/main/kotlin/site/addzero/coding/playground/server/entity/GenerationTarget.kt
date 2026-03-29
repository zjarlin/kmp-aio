package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "generation_target")
interface GenerationTarget {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "project_id")
    val project: CodegenProject

    @IdView
    val projectId: String

    val name: String

    @Column(name = "root_dir")
    val rootDir: String

    @Column(name = "source_set")
    val sourceSet: String

    @Column(name = "base_package")
    val basePackage: String

    @Column(name = "index_package")
    val indexPackage: String

    @Column(name = "ksp_enabled")
    val kspEnabled: Boolean

    @Column(name = "variables_json")
    val variablesJson: String?

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "target")
    val files: List<SourceFileMeta>
}
