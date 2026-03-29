package site.addzero.kcloud.plugins.system.configcenter.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "config_center_project")
interface ConfigCenterProject : BaseEntity {
    @Key
    @Column(name = "project_key")
    val projectKey: String

    val slug: String

    val name: String

    val description: String?

    val enabled: Boolean

    @OneToMany(mappedBy = "project")
    val environments: List<ConfigCenterEnvironment>
}
