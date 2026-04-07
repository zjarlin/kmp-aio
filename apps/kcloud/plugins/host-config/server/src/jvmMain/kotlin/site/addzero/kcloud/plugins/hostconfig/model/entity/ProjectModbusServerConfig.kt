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
interface ProjectModbusServerConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val transportType: TransportType

    val enabled: Boolean

    val tcpPort: Int?

    val portName: String?

    val baudRate: Int?

    val dataBits: Int?

    val stopBits: Int?

    val parity: Parity?

    val stationNo: Int?

    val createdAt: Long

    val updatedAt: Long

    @ManyToOne
    val project: Project
}
