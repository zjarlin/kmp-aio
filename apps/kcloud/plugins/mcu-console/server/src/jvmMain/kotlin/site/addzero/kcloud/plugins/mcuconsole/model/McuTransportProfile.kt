package site.addzero.kcloud.plugins.mcuconsole.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.mcuconsole.McuModbusSerialParity
import site.addzero.kcloud.plugins.mcuconsole.McuTransportKind
import java.time.Instant

/**
 * MCU 连接档案。
 *
 * 用于保存串口连接草稿和 Modbus RTU 复用参数，供页面编辑、回显和最近使用记录复用。
 */
@Entity
@Table(name = "mcu_transport_profile")
interface McuTransportProfile : BaseEntity {
    /** 连接档案稳定主键。 */
    @Key
    @Column(name = "profile_key")
    val profileKey: String

    /** 页面展示名称。 */
    val name: String

    /** 连接类型。 */
    @Column(name = "transport_kind")
    val transportKind: McuTransportKind

    /** 绑定设备键，存在时优先回绑到对应设备。 */
    @Column(name = "device_key")
    val deviceKey: String?

    /** 离线时用于回显的串口路径提示。 */
    @Column(name = "port_path_hint")
    val portPathHint: String?

    /** 串口波特率。 */
    @Column(name = "baud_rate")
    val baudRate: Int?

    /** Modbus 单元 ID。 */
    @Column(name = "unit_id")
    val unitId: Int?

    /** 串口数据位。 */
    @Column(name = "data_bits")
    val dataBits: Int?

    /** 串口停止位。 */
    @Column(name = "stop_bits")
    val stopBits: Int?

    /** 串口奇偶校验。 */
    val parity: McuModbusSerialParity?

    /** 通信超时毫秒数。 */
    @Column(name = "timeout_ms")
    val timeoutMs: Int?

    /** 通信重试次数。 */
    val retries: Int?

    /** 兼容旧表结构保留的主机地址字段，本轮 serial-only 控制台不再使用。 */
    val host: String?

    /** 兼容旧表结构保留的端口字段，本轮 serial-only 控制台不再使用。 */
    val port: Int?

    /** 兼容旧表结构保留的 Client ID 字段，本轮 serial-only 控制台不再使用。 */
    @Column(name = "client_id")
    val clientId: String?

    /** 兼容旧表结构保留的用户名字段，本轮 serial-only 控制台不再使用。 */
    val username: String?

    /** 兼容旧表结构保留的密码字段，本轮 serial-only 控制台不再使用。 */
    val password: String?

    /** 兼容旧表结构保留的发布主题字段，本轮 serial-only 控制台不再使用。 */
    @Column(name = "publish_topic")
    val publishTopic: String?

    /** 兼容旧表结构保留的订阅主题字段，本轮 serial-only 控制台不再使用。 */
    @Column(name = "subscribe_topic")
    val subscribeTopic: String?

    /** 兼容旧表结构保留的 QoS 字段，本轮 serial-only 控制台不再使用。 */
    val qos: Int?

    /** 兼容旧表结构保留的 KeepAlive 字段，本轮 serial-only 控制台不再使用。 */
    @Column(name = "keep_alive_seconds")
    val keepAliveSeconds: Int?

    /** 最近一次成功使用时间。 */
    @Column(name = "last_used_at")
    val lastUsedAt: Instant?
}
