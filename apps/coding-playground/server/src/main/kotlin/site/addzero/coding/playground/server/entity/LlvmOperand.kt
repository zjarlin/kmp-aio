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
@Table(name = "llvm_operand")
interface LlvmOperand {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "instruction_id")
    val instruction: LlvmInstruction

    @IdView
    val instructionId: String

    val kind: String
    val text: String

    @ManyToOne
    @JoinColumn(name = "referenced_instruction_id")
    val referencedInstruction: LlvmInstruction?

    @IdView("referencedInstruction")
    val referencedInstructionId: String?

    @ManyToOne
    @JoinColumn(name = "referenced_function_id")
    val referencedFunction: LlvmFunction?

    @IdView("referencedFunction")
    val referencedFunctionId: String?

    @ManyToOne
    @JoinColumn(name = "referenced_param_id")
    val referencedParam: LlvmFunctionParam?

    @IdView("referencedParam")
    val referencedParamId: String?

    @ManyToOne
    @JoinColumn(name = "referenced_global_id")
    val referencedGlobal: LlvmGlobalVariable?

    @IdView("referencedGlobal")
    val referencedGlobalId: String?

    @ManyToOne
    @JoinColumn(name = "referenced_constant_id")
    val referencedConstant: LlvmConstant?

    @IdView("referencedConstant")
    val referencedConstantId: String?

    @ManyToOne
    @JoinColumn(name = "referenced_block_id")
    val referencedBlock: LlvmBasicBlock?

    @IdView("referencedBlock")
    val referencedBlockId: String?

    @ManyToOne
    @JoinColumn(name = "referenced_metadata_node_id")
    val referencedMetadataNode: LlvmMetadataNode?

    @IdView("referencedMetadataNode")
    val referencedMetadataNodeId: String?

    @ManyToOne
    @JoinColumn(name = "referenced_type_id")
    val referencedType: LlvmType?

    @IdView("referencedType")
    val referencedTypeId: String?

    @ManyToOne
    @JoinColumn(name = "referenced_inline_asm_id")
    val referencedInlineAsm: LlvmInlineAsm?

    @IdView("referencedInlineAsm")
    val referencedInlineAsmId: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}

