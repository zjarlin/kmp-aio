package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "host_config_project_gateway_pin_config")
/**
 * 表示项目网关pin配置。
 */
interface ProjectGatewayPinConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键 ID。
     */
    val id: Long

    /**
     * faultindicatorpin。
     */
    val faultIndicatorPin: String

    /**
     * runningindicatorpin。
     */
    val runningIndicatorPin: String

    /**
     * 创建时间戳。
     */
    val createdAt: Long

    /**
     * 更新时间戳。
     */
    val updatedAt: Long

    @ManyToOne
    /**
     * 项目。
     */
    val project: Project
}
