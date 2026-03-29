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
@Table(name = "config_center_config")
interface ConfigCenterConfig : BaseEntity {
    @Key
    @Column(name = "config_key")
    val configKey: String

    @ManyToOne
    @JoinColumn(name = "project_id")
    val project: ConfigCenterProject

    @ManyToOne
    @JoinColumn(name = "environment_id")
    val environment: ConfigCenterEnvironment

    val slug: String

    val name: String

    @Column(name = "config_type")
    val configType: String

    val description: String?

    val locked: Boolean

    val enabled: Boolean

    @ManyToOne
    @JoinColumn(name = "source_config_id")
    val sourceConfig: ConfigCenterConfig?

    @OneToMany(mappedBy = "config")
    val secrets: List<ConfigCenterSecret>
}
