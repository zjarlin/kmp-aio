package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "host_config_module_template")
interface ModuleTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val code: String

    val name: String

    val description: String?

    val sortIndex: Int

    val channelCount: Int?

    val createdAt: Long

    val updatedAt: Long

    @ManyToOne
    val protocolTemplate: ProtocolTemplate
}
