package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "dto_field_meta")
interface DtoFieldMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "dto_id")
    val dto: DtoMeta

    @IdView
    val dtoId: String

    @ManyToOne
    @JoinColumn(name = "entity_field_id")
    val entityField: FieldMeta?

    @IdView
    val entityFieldId: String?

    val name: String
    val code: String
    val type: String
    val nullable: Boolean
    val list: Boolean

    @Column(name = "source_path")
    val sourcePath: String?

    val description: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
