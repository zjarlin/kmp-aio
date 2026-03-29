package site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "plugin_deployment_artifact")
interface PluginDeploymentArtifact {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "job_id")
    val deploymentJob: PluginDeploymentJob

    @Column(name = "relative_path")
    val relativePath: String

    @Column(name = "absolute_path")
    val absolutePath: String

    @Column(name = "content_hash")
    val contentHash: String

    @Column(name = "created_at")
    val createdAt: LocalDateTime
}
