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
@Table(name = "llvm_constant_item")
interface LlvmConstantItem {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "constant_id")
    val constant: LlvmConstant

    @IdView
    val constantId: String

    @Column(name = "value_text")
    val valueText: String

    @ManyToOne
    @JoinColumn(name = "value_constant_id")
    val valueConstant: LlvmConstant?

    @IdView("valueConstant")
    val valueConstantId: String?

    @ManyToOne
    @JoinColumn(name = "value_type_ref_id")
    val valueTypeRef: LlvmType?

    @IdView("valueTypeRef")
    val valueTypeRefId: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}

