package site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "plugin_package")
interface PluginPackage {
    @Id
    val id: String

    @Column(name = "plugin_id")
    val pluginId: String

    val name: String
    val description: String?
    val version: String

    @Column(name = "plugin_group")
    val pluginGroup: String?

    val enabled: Boolean

    @Column(name = "module_dir")
    val moduleDir: String

    @Column(name = "base_package")
    val basePackage: String

    @Column(name = "managed_by_db")
    val managedByDb: Boolean

    @Column(name = "compose_koin_module_class")
    val composeKoinModuleClass: String?

    @Column(name = "server_koin_module_class")
    val serverKoinModuleClass: String?

    @Column(name = "route_registrar_import")
    val routeRegistrarImport: String?

    @Column(name = "route_registrar_call")
    val routeRegistrarCall: String?

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "pluginPackage")
    val files: List<PluginSourceFile>
}
