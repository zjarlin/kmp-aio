package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "field_meta")
interface FieldMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "entity_id")
    val entity: EntityMeta

    @IdView
    val entityId: String

    val name: String
    val code: String
    val type: String
    val nullable: Boolean
    val list: Boolean

    @Column(name = "id_field")
    val idField: Boolean

    @Column(name = "key_field")
    val keyField: Boolean

    @Column(name = "unique_flag")
    val unique: Boolean

    val searchable: Boolean

    @Column(name = "default_value")
    val defaultValue: String?

    val description: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "entityField")
    val dtoFields: List<DtoFieldMeta>
}
