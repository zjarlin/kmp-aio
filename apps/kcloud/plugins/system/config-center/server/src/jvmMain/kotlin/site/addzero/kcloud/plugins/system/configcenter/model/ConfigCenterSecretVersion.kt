package site.addzero.kcloud.plugins.system.configcenter.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "config_center_secret_version")
interface ConfigCenterSecretVersion : BaseEntity {
    @ManyToOne
    @JoinColumn(name = "secret_id")
    val secret: ConfigCenterSecret

    val version: Int

    val action: String

    @Column(name = "value_text")
    val valueText: String

    @Column(name = "masked_value")
    val maskedValue: String

    val note: String?

    val actor: String
}
