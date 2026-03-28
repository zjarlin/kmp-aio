package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "annotation_usage_meta")
interface AnnotationUsageMeta {
    @Id
    val id: String

    @Column(name = "owner_type")
    val ownerType: String

    @Column(name = "owner_id")
    val ownerId: String

    @Column(name = "annotation_class_name")
    val annotationClassName: String

    @Column(name = "use_site_target")
    val useSiteTarget: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "annotationUsage")
    val arguments: List<AnnotationArgumentMeta>
}
