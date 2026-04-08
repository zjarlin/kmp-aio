package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.DeviceDefinition
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder2
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder4
import site.addzero.kcloud.plugins.hostconfig.model.enums.FloatOrder

@Entity
@Table(name = "host_config_device")
interface Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val name: String

    val stationNo: Int

    val requestIntervalMs: Int?

    val writeIntervalMs: Int?

    val byteOrder2: ByteOrder2?

    val byteOrder4: ByteOrder4?

    val floatOrder: FloatOrder?

    val batchAnalogStart: Int?

    val batchAnalogLength: Int?

    val batchDigitalStart: Int?

    val batchDigitalLength: Int?

    val disabled: Boolean

    val sortIndex: Int

    val createdAt: Long

    val updatedAt: Long

    @ManyToOne
    val module: ModuleInstance

    @ManyToOne
    val deviceType: DeviceType

    @ManyToOne
    val deviceDefinition: DeviceDefinition?

    @OneToMany(mappedBy = "device")
    val tags: List<Tag>
}
