package site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "plugin_deployment_job")
interface PluginDeploymentJob {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "package_id")
    val pluginPackage: PluginPackage

    val status: String

    @Column(name = "exported_module_dir")
    val exportedModuleDir: String

    @Column(name = "build_command")
    val buildCommand: String?

    @Column(name = "stdout_text")
    val stdoutText: String?

    @Column(name = "stderr_text")
    val stderrText: String?

    @Column(name = "summary_text")
    val summaryText: String?

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
