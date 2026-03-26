package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "llvm_type")
interface LlvmType {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "module_id")
    val module: LlvmModule

    @IdView
    val moduleId: String

    val name: String
    val symbol: String
    val kind: String

    @Column(name = "primitive_width")
    val primitiveWidth: Int?

    val packed: Boolean
    val opaque: Boolean

    @Column(name = "address_space")
    val addressSpace: Int?

    @Column(name = "array_length")
    val arrayLength: Int?

    val scalable: Boolean
    val variadic: Boolean

    @Column(name = "definition_text")
    val definitionText: String?

    @ManyToOne
    @JoinColumn(name = "element_type_ref_id")
    val elementTypeRef: LlvmType?

    @IdView("elementTypeRef")
    val elementTypeRefId: String?

    @ManyToOne
    @JoinColumn(name = "return_type_ref_id")
    val returnTypeRef: LlvmType?

    @IdView("returnTypeRef")
    val returnTypeRefId: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "type")
    val members: List<LlvmTypeMember>
}

