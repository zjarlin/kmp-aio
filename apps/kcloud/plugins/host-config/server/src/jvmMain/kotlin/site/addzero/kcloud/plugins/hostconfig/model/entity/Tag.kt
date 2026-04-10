package site.addzero.kcloud.plugins.hostconfig.model.entity

import java.math.BigDecimal
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.hostconfig.model.enums.PointType

@Entity
@Table(name = "host_config_tag")
/**
 * 定义标签实体。
 */
interface Tag : BaseEntity {

    /**
     * 名称。
     */
    val name: String

    /**
     * 描述。
     */
    val description: String?

    /**
     * register地址。
     */
    val registerAddress: Int

    /**
     * 是否启用。
     */
    val enabled: Boolean

    /**
     * 默认值。
     */
    val defaultValue: String?

    /**
     * 异常值。
     */
    val exceptionValue: String?

    /**
     * point类型。
     */
    val pointType: PointType?

    /**
     * debounce毫秒。
     */
    val debounceMs: Int?

    /**
     * 排序序号。
     */
    val sortIndex: Int

    /**
     * scaling启用状态。
     */
    val scalingEnabled: Boolean

    /**
     * scalingoffset。
     */
    val scalingOffset: BigDecimal?

    /**
     * rawmin。
     */
    val rawMin: BigDecimal?

    /**
     * rawmax。
     */
    val rawMax: BigDecimal?

    /**
     * engmin。
     */
    val engMin: BigDecimal?

    /**
     * engmax。
     */
    val engMax: BigDecimal?

    /**
     * forward启用状态。
     */
    val forwardEnabled: Boolean

    /**
     * forwardregister地址。
     */
    val forwardRegisterAddress: Int?

    @ManyToOne
    /**
     * 设备。
     */
    val device: Device

    @ManyToOne
    /**
     * 数据类型。
     */
    val dataType: DataType

    @ManyToOne
    /**
     * register类型。
     */
    val registerType: RegisterType

    @ManyToOne
    /**
     * forwardregister类型。
     */
    val forwardRegisterType: RegisterType?

    @OneToMany(mappedBy = "tag")
    /**
     * 值texts。
     */
    val valueTexts: List<TagValueText>
}
