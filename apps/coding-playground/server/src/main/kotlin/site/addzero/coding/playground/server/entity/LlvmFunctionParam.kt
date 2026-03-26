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
@Table(name = "llvm_function_param")
interface LlvmFunctionParam {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "function_id")
    val function: LlvmFunction

    @IdView
    val functionId: String

    val name: String

    @Column(name = "type_text")
    val typeText: String

    @ManyToOne
    @JoinColumn(name = "type_ref_id")
    val typeRef: LlvmType?

    @IdView("typeRef")
    val typeRefId: String?

    @Column(name = "attributes_json")
    val attributesJson: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}

