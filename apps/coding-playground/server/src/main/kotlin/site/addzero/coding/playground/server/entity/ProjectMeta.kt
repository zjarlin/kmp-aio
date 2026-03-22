package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "project_meta")
interface ProjectMeta {
    @Id
    val id: String

    val name: String
    val slug: String
    val description: String?

    @Column(name = "tags_json")
    val tagsJson: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "project")
    val contexts: List<BoundedContextMeta>

    @OneToMany(mappedBy = "project")
    val generationTargets: List<GenerationTargetMeta>

    @OneToMany(mappedBy = "project")
    val etlWrappers: List<EtlWrapperMeta>
}
