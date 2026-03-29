package site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "plugin_import_record")
interface PluginImportRecord {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "package_id")
    val pluginPackage: PluginPackage

    @Column(name = "source_module_dir")
    val sourceModuleDir: String

    @Column(name = "source_gradle_path")
    val sourceGradlePath: String

    @Column(name = "imported_at")
    val importedAt: LocalDateTime
}
