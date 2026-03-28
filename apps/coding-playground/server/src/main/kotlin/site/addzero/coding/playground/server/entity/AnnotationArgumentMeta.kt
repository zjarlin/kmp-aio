package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "annotation_argument_meta")
interface AnnotationArgumentMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "annotation_usage_id")
    val annotationUsage: AnnotationUsageMeta

    @IdView
    val annotationUsageId: String

    val name: String?
    val value: String

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
