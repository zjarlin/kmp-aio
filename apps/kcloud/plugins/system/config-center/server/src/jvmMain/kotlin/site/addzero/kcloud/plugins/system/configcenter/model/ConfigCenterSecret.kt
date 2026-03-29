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
@Table(name = "config_center_secret")
interface ConfigCenterSecret : BaseEntity {
    @Key
    @Column(name = "secret_key")
    val secretKey: String

    @ManyToOne
    @JoinColumn(name = "project_id")
    val project: ConfigCenterProject

    @ManyToOne
    @JoinColumn(name = "config_id")
    val config: ConfigCenterConfig

    val name: String

    @Column(name = "value_text")
    val valueText: String

    @Column(name = "masked_value")
    val maskedValue: String

    val note: String?

    @Column(name = "value_type")
    val valueType: String

    val sensitive: Boolean

    val enabled: Boolean

    val deleted: Boolean

    val version: Int

    @OneToMany(mappedBy = "secret")
    val versions: List<ConfigCenterSecretVersion>
}
