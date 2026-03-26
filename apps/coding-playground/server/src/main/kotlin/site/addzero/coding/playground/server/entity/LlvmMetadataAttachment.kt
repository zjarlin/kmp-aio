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
@Table(name = "llvm_metadata_attachment")
interface LlvmMetadataAttachment {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "metadata_node_id")
    val metadataNode: LlvmMetadataNode

    @IdView
    val metadataNodeId: String

    @Column(name = "target_kind")
    val targetKind: String

    @ManyToOne
    @JoinColumn(name = "function_id")
    val function: LlvmFunction?

    @IdView("function")
    val functionId: String?

    @ManyToOne
    @JoinColumn(name = "global_variable_id")
    val globalVariable: LlvmGlobalVariable?

    @IdView("globalVariable")
    val globalVariableId: String?

    @ManyToOne
    @JoinColumn(name = "instruction_id")
    val instruction: LlvmInstruction?

    @IdView("instruction")
    val instructionId: String?

    val key: String

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}

