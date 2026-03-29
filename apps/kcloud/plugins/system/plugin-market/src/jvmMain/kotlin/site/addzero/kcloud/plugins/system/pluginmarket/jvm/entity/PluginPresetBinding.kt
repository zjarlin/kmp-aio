package site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
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
