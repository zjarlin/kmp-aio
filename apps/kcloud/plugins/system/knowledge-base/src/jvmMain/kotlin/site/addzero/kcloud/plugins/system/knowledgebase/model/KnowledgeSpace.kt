package site.addzero.kcloud.plugins.system.knowledgebase.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "knowledge_space")
interface KnowledgeSpace : BaseEntity {
    @Key
    @Column(name = "space_key")
    val spaceKey: String

    val name: String

    val description: String?

    @OneToMany(mappedBy = "space")
    val documents: List<KnowledgeDocument>
}
