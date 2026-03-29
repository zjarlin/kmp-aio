package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "source_file_meta")
interface SourceFileMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "project_id")
    val project: CodegenProject

    @IdView
    val projectId: String

    @ManyToOne
    @JoinColumn(name = "target_id")
    val target: GenerationTarget

    @IdView
    val targetId: String

    @Column(name = "package_name")
    val packageName: String

    @Column(name = "file_name")
    val fileName: String

    @Column(name = "doc_comment")
    val docComment: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "file")
    val declarations: List<DeclarationMeta>

    @OneToMany(mappedBy = "file")
    val imports: List<ImportMeta>

    @OneToMany(mappedBy = "file")
    val artifacts: List<ManagedArtifactMeta>
}
