package site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "plugin_preset_binding")
interface PluginPresetBinding {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "package_id")
    val pluginPackage: PluginPackage

    @Column(name = "preset_kind")
    val presetKind: String

    @Column(name = "applied_at")
    val appliedAt: LocalDateTime
}
