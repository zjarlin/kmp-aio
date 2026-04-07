package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "host_config_protocol_template")
interface ProtocolTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val code: String

    val name: String

    val description: String?

    val sortIndex: Int

    val createdAt: Long

    val updatedAt: Long

    @OneToMany(mappedBy = "protocolTemplate")
    val moduleTemplates: List<ModuleTemplate>
}
