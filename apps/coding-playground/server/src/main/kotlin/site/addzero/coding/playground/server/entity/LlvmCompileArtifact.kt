package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "llvm_compile_artifact")
interface LlvmCompileArtifact {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "job_id")
    val job: LlvmCompileJob

    @IdView
    val jobId: String

    val kind: String

    @Column(name = "file_path")
    val filePath: String

    @Column(name = "size_bytes")
    val sizeBytes: Long

    @Column(name = "created_at")
    val createdAt: LocalDateTime
}
