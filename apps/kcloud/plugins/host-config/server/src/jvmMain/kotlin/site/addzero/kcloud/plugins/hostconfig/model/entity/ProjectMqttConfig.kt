package site.addzero.kcloud.plugins.hostconfig.model.entity

import java.math.BigDecimal
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "host_config_project_mqtt_config")
/**
 * 表示项目MQTT配置。
 */
interface ProjectMqttConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键 ID。
     */
    val id: Long

    /**
     * 是否启用。
     */
    val enabled: Boolean

    /**
     * breakpointresume。
     */
    val breakpointResume: Boolean

    /**
     * 网关名称。
     */
    val gatewayName: String?

    /**
     * vendor。
     */
    val vendor: String?

    /**
     * 主机地址。
     */
    val host: String?

    /**
     * 端口。
     */
    val port: Int?

    /**
     * 主题。
     */
    val topic: String?

    /**
     * 网关 ID。
     */
    val gatewayId: String?

    /**
     * auth启用状态。
     */
    val authEnabled: Boolean

    /**
     * username。
     */
    val username: String?

    /**
     * passwordencrypted。
     */
    val passwordEncrypted: String?

    /**
     * tls启用状态。
     */
    val tlsEnabled: Boolean

    /**
     * certfileref。
     */
    val certFileRef: String?

    /**
     * 客户端 ID。
     */
    val clientId: String?

    /**
     * keepalivesec。
     */
    val keepAliveSec: Int?

    /**
     * QoS 等级。
     */
    val qos: Int?

    /**
     * reportperiodsec。
     */
    val reportPeriodSec: Int?

    @Column(name = "precision_value")
    /**
     * precision。
     */
    val precision: BigDecimal?

    /**
     * 值changeratio启用状态。
     */
    val valueChangeRatioEnabled: Boolean

    /**
     * 云接入控制disabled。
     */
    val cloudControlDisabled: Boolean

    /**
     * 创建时间戳。
     */
    val createdAt: Long

    /**
     * 更新时间戳。
     */
    val updatedAt: Long

    @OneToOne
    /**
     * 项目。
     */
    val project: Project
}
