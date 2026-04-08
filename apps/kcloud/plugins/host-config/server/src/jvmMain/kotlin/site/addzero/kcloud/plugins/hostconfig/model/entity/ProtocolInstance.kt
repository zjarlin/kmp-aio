package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToManyView
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Entity
@Table(name = "host_config_protocol_instance")
interface ProtocolInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val name: String

    val pollingIntervalMs: Int

    val transportType: TransportType?

    val host: String?

    val tcpPort: Int?

    val portName: String?

    val baudRate: Int?

    val dataBits: Int?

    val stopBits: Int?

    val parity: Parity?

    val responseTimeoutMs: Int?

    val createdAt: Long

    val updatedAt: Long

    @ManyToOne
    val protocolTemplate: ProtocolTemplate

    @OneToMany(mappedBy = "protocol")
    val projectLinks: List<ProjectProtocol>

    @ManyToManyView(prop = "projectLinks", deeperProp = "project")
    val projects: List<Project>

    @OneToMany(mappedBy = "protocol")
    val modules: List<ModuleInstance>
}
