package site.addzero.kcloud.plugins.system.configcenter.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "config_center_environment")
interface ConfigCenterEnvironment : BaseEntity {
    @Key
    @Column(name = "environment_key")
    val environmentKey: String

    @ManyToOne
    @JoinColumn(name = "project_id")
    val project: ConfigCenterProject

    val slug: String

    val name: String

    val description: String?

    @Column(name = "sort_order")
    val sortOrder: Int

    @Column(name = "is_default")
    val isDefault: Boolean

    @Column(name = "personal_config_enabled")
    val personalConfigEnabled: Boolean

    @OneToMany(mappedBy = "environment")
    val configs: List<ConfigCenterConfig>
}
