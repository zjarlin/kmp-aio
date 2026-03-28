package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "sync_conflict_meta")
interface SyncConflictMeta {
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

    @ManyToOne
    @JoinColumn(name = "file_id")
    val file: SourceFileMeta

    @IdView
    val fileId: String

    @ManyToOne
    @JoinColumn(name = "artifact_id")
    val artifact: ManagedArtifactMeta?

    @IdView("artifact")
    val artifactId: String?

    val reason: String
    val message: String

    @Column(name = "metadata_hash")
    val metadataHash: String

    @Column(name = "source_hash")
    val sourceHash: String?

    @Column(name = "source_path")
    val sourcePath: String?

    val resolved: Boolean
    val resolution: String?

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
