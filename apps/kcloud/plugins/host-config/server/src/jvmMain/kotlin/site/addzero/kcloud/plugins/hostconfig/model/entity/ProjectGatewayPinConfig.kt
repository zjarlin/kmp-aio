package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.EpochBaseEntity

@Entity
@Table(name = "host_config_project_gateway_pin_config")
/**
 * 表示项目网关pin配置。
 */
interface ProjectGatewayPinConfig : EpochBaseEntity {

    /**
     * faultindicatorpin。
     */
    val faultIndicatorPin: String

    /**
     * runningindicatorpin。
     */
    val runningIndicatorPin: String

    @ManyToOne
    /**
     * 项目。
     */
    val project: Project
}
