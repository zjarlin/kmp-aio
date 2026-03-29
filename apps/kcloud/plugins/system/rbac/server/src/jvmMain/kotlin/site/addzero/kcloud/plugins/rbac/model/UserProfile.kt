package site.addzero.kcloud.plugins.rbac.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "user_profile")
interface UserProfile : BaseEntity {
    @Key
    @Column(name = "account_key")
    val accountKey: String

    @Column(name = "display_name")
    val displayName: String

    val email: String?

    @Column(name = "avatar_label")
    val avatarLabel: String

    val locale: String

    @Column(name = "time_zone")
    val timeZone: String
}
