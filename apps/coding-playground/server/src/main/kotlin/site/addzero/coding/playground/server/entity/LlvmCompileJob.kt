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
@Table(name = "llvm_compile_job")
interface LlvmCompileJob {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "module_id")
    val module: LlvmModule

    @IdView
    val moduleId: String

    @ManyToOne
    @JoinColumn(name = "profile_id")
    val profile: LlvmCompileProfile

    @IdView("profile")
    val profileId: String

    val status: String

    @Column(name = "output_directory")
    val outputDirectory: String

    @Column(name = "export_path")
    val exportPath: String?

    @Column(name = "stdout_text")
    val stdoutText: String?

    @Column(name = "stderr_text")
    val stderrText: String?

    @Column(name = "exit_code")
    val exitCode: Int?

    @Column(name = "finished_at")
    val finishedAt: LocalDateTime?

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "job")
    val artifacts: List<LlvmCompileArtifact>
}

