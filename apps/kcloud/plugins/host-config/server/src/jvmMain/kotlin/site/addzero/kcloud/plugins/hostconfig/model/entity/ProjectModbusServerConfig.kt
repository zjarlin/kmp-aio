package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Entity
@Table(name = "host_config_project_modbus_server_config")
/**
 * 表示项目modbus服务端配置。
 */
interface ProjectModbusServerConfig : BaseEntity {

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

    @ManyToOne
    /**
     * 项目。
     */
    val project: Project
}
