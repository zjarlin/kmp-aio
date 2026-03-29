package site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "plugin_source_file")
interface PluginSourceFile {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "package_id")
    val pluginPackage: PluginPackage

    @Column(name = "relative_path")
    val relativePath: String

    val content: String

    @Column(name = "content_hash")
    val contentHash: String

    @Column(name = "file_group")
    val fileGroup: String

    @Column(name = "read_only")
    val readOnly: Boolean

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
