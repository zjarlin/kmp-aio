package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "host_config_feature_definition")
interface FeatureDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val identifier: String

    val name: String

    val description: String?

    val inputSchema: String?

    val outputSchema: String?

    val asynchronous: Boolean

    val sortIndex: Int

    val createdAt: Long

    val updatedAt: Long

    @ManyToOne
    val deviceDefinition: DeviceDefinition
}
