package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity

@Entity
@Table(name = "host_config_module_instance")
interface ModuleInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val name: String

    val portName: String?

    val baudRate: Int?

    val dataBits: Int?

    val stopBits: Int?

    val parity: Parity?

    val responseTimeoutMs: Int?

    val sortIndex: Int

    val createdAt: Long

    val updatedAt: Long

    @ManyToOne
    val protocol: ProtocolInstance

    @ManyToOne
    val moduleTemplate: ModuleTemplate

    @OneToMany(mappedBy = "module")
    val devices: List<Device>
}
