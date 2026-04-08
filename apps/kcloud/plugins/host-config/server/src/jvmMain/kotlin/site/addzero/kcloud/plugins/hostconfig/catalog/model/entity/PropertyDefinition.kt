package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.plugins.hostconfig.model.entity.DataType

@Entity
@Table(name = "host_config_property_definition")
interface PropertyDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val identifier: String

    val name: String

    val description: String?

    val unit: String?

    val required: Boolean

    val writable: Boolean

    val telemetry: Boolean

    val nullable: Boolean

    val length: Int?

    val attributesJson: String?

    val sortIndex: Int

    val createdAt: Long

    val updatedAt: Long

    @ManyToOne
    val deviceDefinition: DeviceDefinition

    @ManyToOne
    val dataType: DataType
}
