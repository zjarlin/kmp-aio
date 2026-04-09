package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToManyView
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.EpochBaseEntity

@Entity
@Table(name = "host_config_project")
/**
 * 定义项目实体。
 */
interface Project : EpochBaseEntity {

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
