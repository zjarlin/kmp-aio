package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.plugins.hostconfig.model.entity.DeviceType

@Entity
@Table(name = "host_config_device_definition")
interface DeviceDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val code: String

    val name: String

    val description: String?

    val supportsTelemetry: Boolean

    val supportsControl: Boolean

    val sortIndex: Int

    val createdAt: Long

    val updatedAt: Long

    @ManyToOne
    val product: ProductDefinition

    @ManyToOne
    val deviceType: DeviceType?

    @OneToMany(mappedBy = "deviceDefinition")
    val properties: List<PropertyDefinition>

    @OneToMany(mappedBy = "deviceDefinition")
    val features: List<FeatureDefinition>
}
