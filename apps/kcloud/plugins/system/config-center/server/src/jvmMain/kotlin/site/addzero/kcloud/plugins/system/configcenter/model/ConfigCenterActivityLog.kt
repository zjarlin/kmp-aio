package site.addzero.kcloud.plugins.system.configcenter.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "config_center_activity_log")
interface ConfigCenterActivityLog : BaseEntity {
    @ManyToOne
    @JoinColumn(name = "project_id")
    val project: ConfigCenterProject

    @ManyToOne
    @JoinColumn(name = "config_id")
    val config: ConfigCenterConfig?

    val action: String

    @Column(name = "resource_type")
    val resourceType: String

    @Column(name = "resource_key")
    val resourceKey: String

    val summary: String

    @Column(name = "detail_json")
    val detailJson: String?

    val actor: String
}
