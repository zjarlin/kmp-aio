package site.addzero.kcloud.plugins.system.configcenter.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import java.time.Instant

@Entity
@Table(name = "config_center_service_token")
interface ConfigCenterServiceToken : BaseEntity {
    @Key
    @Column(name = "token_key")
    val tokenKey: String

    @ManyToOne
    @JoinColumn(name = "project_id")
    val project: ConfigCenterProject

    @ManyToOne
    @JoinColumn(name = "config_id")
    val config: ConfigCenterConfig

    val name: String

    @Column(name = "token_hash")
    val tokenHash: String

    @Column(name = "token_prefix")
    val tokenPrefix: String

    @Column(name = "write_access")
    val writeAccess: Boolean

    val description: String?

    val active: Boolean

    @Column(name = "last_used_time")
    val lastUsedTime: Instant?

    @Column(name = "expire_time")
    val expireTime: Instant?

    @Column(name = "revoke_time")
    val revokeTime: Instant?
}
