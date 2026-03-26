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
@Table(name = "llvm_phi_incoming")
interface LlvmPhiIncoming {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "instruction_id")
    val instruction: LlvmInstruction

    @IdView
    val instructionId: String

    @Column(name = "value_text")
    val valueText: String

    @ManyToOne
    @JoinColumn(name = "value_operand_id")
    val valueOperand: LlvmOperand?

    @IdView("valueOperand")
    val valueOperandId: String?

    @ManyToOne
    @JoinColumn(name = "incoming_block_id")
    val incomingBlock: LlvmBasicBlock

    @IdView("incomingBlock")
    val incomingBlockId: String

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}

