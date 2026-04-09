package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Entity
@Table(name = "host_config_project_modbus_server_config")
/**
 * 表示项目modbus服务端配置。
 */
interface ProjectModbusServerConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键 ID。
     */
    val id: Long

    /**
     * 传输类型。
     */
    val transportType: TransportType

    /**
     * 是否启用。
     */
    val enabled: Boolean

    /**
     * TCP端口。
     */
    val tcpPort: Int?

    /**
     * 端口名。
     */
    val portName: String?

    /**
     * 波特率。
     */
    val baudRate: Int?

    /**
     * 数据位。
     */
    val dataBits: Int?

    /**
     * 停止位。
     */
    val stopBits: Int?

    /**
     * 校验位。
     */
    val parity: Parity?

    /**
     * stationno。
     */
    val stationNo: Int?

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
