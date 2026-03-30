package site.addzero.kcloud.plugins.mcuconsole.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.mcuconsole.McuModbusSerialParity
import site.addzero.kcloud.plugins.mcuconsole.McuTransportKind
import java.time.Instant

@Entity
@Table(name = "mcu_transport_profile")
interface McuTransportProfile : BaseEntity {
    @Key
    @Column(name = "profile_key")
    val profileKey: String

    val name: String

    @Column(name = "transport_kind")
    val transportKind: McuTransportKind

    @Column(name = "device_key")
    val deviceKey: String?

    @Column(name = "port_path_hint")
    val portPathHint: String?

    @Column(name = "baud_rate")
    val baudRate: Int?

    @Column(name = "unit_id")
    val unitId: Int?

    @Column(name = "data_bits")
    val dataBits: Int?

    @Column(name = "stop_bits")
    val stopBits: Int?

    val parity: McuModbusSerialParity?

    @Column(name = "timeout_ms")
    val timeoutMs: Int?

    val retries: Int?

    val host: String?

    val port: Int?

    @Column(name = "client_id")
    val clientId: String?

    val username: String?

    val password: String?

    @Column(name = "publish_topic")
    val publishTopic: String?

    @Column(name = "subscribe_topic")
    val subscribeTopic: String?

    val qos: Int?

    @Column(name = "keep_alive_seconds")
    val keepAliveSeconds: Int?

    @Column(name = "last_used_at")
    val lastUsedAt: Instant?
}
