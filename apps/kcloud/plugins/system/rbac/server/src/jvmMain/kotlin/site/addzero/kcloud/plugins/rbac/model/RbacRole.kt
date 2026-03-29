package site.addzero.kcloud.plugins.rbac.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "rbac_role")
interface RbacRole : BaseEntity {
    @Key
    @Column(name = "role_key")
    val roleKey: String

    @Column(name = "role_code")
    val roleCode: String

    val name: String

    val description: String?

    @Column(name = "built_in")
    val builtIn: Boolean

    val enabled: Boolean
}
