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
@Table(name = "llvm_instruction")
interface LlvmInstruction {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "block_id")
    val block: LlvmBasicBlock

    @IdView
    val blockId: String

    val opcode: String

    @Column(name = "result_symbol")
    val resultSymbol: String?

    @Column(name = "type_text")
    val typeText: String?

    @ManyToOne
    @JoinColumn(name = "type_ref_id")
    val typeRef: LlvmType?

    @IdView("typeRef")
    val typeRefId: String?

    @Column(name = "text_suffix")
    val textSuffix: String?

    @Column(name = "flags_json")
    val flagsJson: String?

    val terminator: Boolean

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "instruction")
    val operands: List<LlvmOperand>

    @OneToMany(mappedBy = "instruction")
    val phiIncomings: List<LlvmPhiIncoming>

    @OneToMany(mappedBy = "instruction")
    val clauses: List<LlvmInstructionClause>

    @OneToMany(mappedBy = "instruction")
    val bundles: List<LlvmOperandBundle>
}

