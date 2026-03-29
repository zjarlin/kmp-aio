package site.addzero.kcloud.plugins.system.knowledgebase.model

import org.babyfish.jimmer.sql.*
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
