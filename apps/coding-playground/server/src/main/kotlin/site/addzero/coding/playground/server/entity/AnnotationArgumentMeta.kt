package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
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
