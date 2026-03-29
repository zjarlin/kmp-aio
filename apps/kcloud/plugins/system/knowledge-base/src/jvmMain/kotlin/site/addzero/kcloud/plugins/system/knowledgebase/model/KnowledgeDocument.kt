package site.addzero.kcloud.plugins.system.knowledgebase.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "knowledge_document")
interface KnowledgeDocument : BaseEntity {
    @Key
    @Column(name = "document_key")
    val documentKey: String

    @ManyToOne
    @JoinColumn(name = "space_id")
    val space: KnowledgeSpace

    val title: String

    val content: String
}
