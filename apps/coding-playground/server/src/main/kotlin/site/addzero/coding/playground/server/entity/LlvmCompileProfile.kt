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
@Table(name = "llvm_compile_profile")
interface LlvmCompileProfile {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "module_id")
    val module: LlvmModule

    @IdView
    val moduleId: String

    val name: String

    @Column(name = "target_platform")
    val targetPlatform: String

    @Column(name = "output_directory")
    val outputDirectory: String

    @Column(name = "opt_path")
    val optPath: String?

    @Column(name = "opt_args_json")
    val optArgsJson: String?

    @Column(name = "llc_path")
    val llcPath: String?

    @Column(name = "llc_args_json")
    val llcArgsJson: String?

    @Column(name = "clang_path")
    val clangPath: String?

    @Column(name = "clang_args_json")
    val clangArgsJson: String?

    @Column(name = "environment_json")
    val environmentJson: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}

