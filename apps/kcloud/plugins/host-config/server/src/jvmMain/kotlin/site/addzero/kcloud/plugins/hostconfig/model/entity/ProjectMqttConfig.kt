package site.addzero.kcloud.plugins.hostconfig.model.entity

import java.math.BigDecimal
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "host_config_project_mqtt_config")
/**
 * 表示项目MQTT配置。
 */
interface ProjectMqttConfig : BaseEntity {

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

    @OneToOne
    /**
     * 项目。
     */
    val project: Project
}
