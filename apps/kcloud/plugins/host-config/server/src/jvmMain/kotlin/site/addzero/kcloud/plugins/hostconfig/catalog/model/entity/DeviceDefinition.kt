package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.EpochBaseEntity
import site.addzero.kcloud.plugins.hostconfig.model.entity.DeviceType

/**
 * 设备定义实体。
 *
 * 一个产品下可以声明多个设备型号定义，
 * 用来描述该型号支持的属性、功能和设备类型归属。
 */
@Entity
@Table(name = "host_config_device_definition")
interface DeviceDefinition : EpochBaseEntity {

    /** 设备业务编码，在所属产品内唯一。 */
    val code: String

    /** 设备显示名称。 */
    val name: String

    /** 设备说明。 */
    val description: String?

    /** 是否支持遥测属性采集。 */
    val supportsTelemetry: Boolean

    /** 是否支持控制功能下发。 */
    val supportsControl: Boolean

    /** 同级排序值，越小越靠前。 */
    val sortIndex: Int

    /** 所属产品定义。 */
    @ManyToOne
    val product: ProductDefinition

    /** 设备类型模板，可为空，表示尚未绑定类型字典。 */
    @ManyToOne
    val deviceType: DeviceType?

    /** 当前设备型号下的属性定义集合。 */
    @OneToMany(mappedBy = "deviceDefinition")
    val properties: List<PropertyDefinition>

    /** 当前设备型号下的功能定义集合。 */
    @OneToMany(mappedBy = "deviceDefinition")
    val features: List<FeatureDefinition>
}
