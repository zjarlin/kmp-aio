package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToManyView
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "host_config_project")
/**
 * 定义项目实体。
 */
interface Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键 ID。
     */
    val id: Long

    @Key
    /**
     * 名称。
     */
    val name: String

    /**
     * 描述。
     */
    val description: String?

    /**
     * 备注。
     */
    val remark: String?

    /**
     * 排序序号。
     */
    val sortIndex: Int

    /**
     * 创建时间戳。
     */
    val createdAt: Long

    /**
     * 更新时间戳。
     */
    val updatedAt: Long

    @OneToMany(mappedBy = "project")
    /**
     * 协议关联。
     */
    val protocolLinks: List<ProjectProtocol>

    @ManyToManyView(prop = "protocolLinks", deeperProp = "protocol")
    /**
     * 协议。
     */
    val protocols: List<ProtocolInstance>

    @OneToOne(mappedBy = "project")
    /**
     * MQTT配置。
     */
    val mqttConfig: ProjectMqttConfig?

    @OneToMany(mappedBy = "project")
    /**
     * modbus服务端配置。
     */
    val modbusServerConfigs: List<ProjectModbusServerConfig>
}
