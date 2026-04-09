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
/**
 * 定义协议instance实体。
 */
interface ProtocolInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键 ID。
     */
    val id: Long

    /**
     * 名称。
     */
    val name: String

    /**
     * 轮询间隔（毫秒）。
     */
    val pollingIntervalMs: Int

    /**
     * 传输类型。
     */
    val transportType: TransportType?

    /**
     * 主机地址。
     */
    val host: String?

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
     * 响应超时时间（毫秒）。
     */
    val responseTimeoutMs: Int?

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
     * 协议模板。
     */
    val protocolTemplate: ProtocolTemplate

    @OneToMany(mappedBy = "protocol")
    /**
     * 项目关联。
     */
    val projectLinks: List<ProjectProtocol>

    @ManyToManyView(prop = "projectLinks", deeperProp = "project")
    /**
     * 项目。
     */
    val projects: List<Project>

    @OneToMany(mappedBy = "protocol")
    /**
     * 模块。
     */
    val modules: List<ModuleInstance>
}
