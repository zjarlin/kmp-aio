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
@Table(name = "managed_artifact_meta")
interface ManagedArtifactMeta {
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

    @Column(name = "declaration_ids_json")
    val declarationIdsJson: String?

    @Column(name = "absolute_path")
    val absolutePath: String

    @Column(name = "marker_text")
    val markerText: String

    @Column(name = "metadata_hash")
    val metadataHash: String

    @Column(name = "source_hash")
    val sourceHash: String?

    @Column(name = "content_hash")
    val contentHash: String

    @Column(name = "sync_status")
    val syncStatus: String

    @Column(name = "last_exported_at")
    val lastExportedAt: LocalDateTime?

    @Column(name = "last_imported_at")
    val lastImportedAt: LocalDateTime?

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
