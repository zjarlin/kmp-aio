package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.EpochBaseEntity
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder2
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder4
import site.addzero.kcloud.plugins.hostconfig.model.enums.FloatOrder

@Entity
@Table(name = "host_config_device")
/**
 * 定义设备实体。
 */
interface Device : EpochBaseEntity {

    /**
     * 名称。
     */
    val name: String

    /**
     * stationno。
     */
    val stationNo: Int

    /**
     * 请求间隔（毫秒）。
     */
    val requestIntervalMs: Int?

    /**
     * 写入间隔（毫秒）。
     */
    val writeIntervalMs: Int?

    /**
     * 双字节字节序。
     */
    val byteOrder2: ByteOrder2?

    /**
     * 四字节字节序。
     */
    val byteOrder4: ByteOrder4?

    /**
     * 浮点字序。
     */
    val floatOrder: FloatOrder?

    /**
     * 批量analog开始。
     */
    val batchAnalogStart: Int?

    /**
     * 批量analoglength。
     */
    val batchAnalogLength: Int?

    /**
     * 批量digital开始。
     */
    val batchDigitalStart: Int?

    /**
     * 批量digitallength。
     */
    val batchDigitalLength: Int?

    /**
     * disabled。
     */
    val disabled: Boolean

    /**
     * 排序序号。
     */
    val sortIndex: Int

    @ManyToOne
    /**
     * 模块。
     */
    val module: ModuleInstance

    @ManyToOne
    /**
     * 设备类型。
     */
    val deviceType: DeviceType

    @OneToMany(mappedBy = "device")
    /**
     * 标签。
     */
    val tags: List<Tag>
}
