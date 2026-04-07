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
interface ProjectMqttConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val enabled: Boolean

    val breakpointResume: Boolean

    val gatewayName: String?

    val vendor: String?

    val host: String?

    val port: Int?

    val topic: String?

    val gatewayId: String?

    val authEnabled: Boolean

    val username: String?

    val passwordEncrypted: String?

    val tlsEnabled: Boolean

    val certFileRef: String?

    val clientId: String?

    val keepAliveSec: Int?

    val qos: Int?

    val reportPeriodSec: Int?

    @Column(name = "precision_value")
    val precision: BigDecimal?

    val valueChangeRatioEnabled: Boolean

    val cloudControlDisabled: Boolean

    val createdAt: Long

    val updatedAt: Long

    @OneToOne
    val project: Project
}
