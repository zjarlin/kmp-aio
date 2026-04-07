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
interface Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val name: String

    val description: String?

    val remark: String?

    val sortIndex: Int

    val createdAt: Long

    val updatedAt: Long

    @OneToMany(mappedBy = "project")
    val protocolLinks: List<ProjectProtocol>

    @ManyToManyView(prop = "protocolLinks", deeperProp = "protocol")
    val protocols: List<ProtocolInstance>

    @OneToOne(mappedBy = "project")
    val mqttConfig: ProjectMqttConfig?

    @OneToMany(mappedBy = "project")
    val modbusServerConfigs: List<ProjectModbusServerConfig>
}
