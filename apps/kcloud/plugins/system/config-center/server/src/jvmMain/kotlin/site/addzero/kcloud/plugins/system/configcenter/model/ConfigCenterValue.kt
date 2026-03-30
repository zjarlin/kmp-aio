package site.addzero.kcloud.plugins.system.configcenter.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "config_center_value")
interface ConfigCenterValue : BaseEntity {
    val namespace: String

    @Column(name = "active_profile")
    val active: String

    @Column(name = "config_key")
    val configKey: String

    @Column(name = "config_value")
    val configValue: String
}
