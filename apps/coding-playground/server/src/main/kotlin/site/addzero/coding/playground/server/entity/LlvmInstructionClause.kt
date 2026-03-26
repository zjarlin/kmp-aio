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
@Table(name = "llvm_instruction_clause")
interface LlvmInstructionClause {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "instruction_id")
    val instruction: LlvmInstruction

    @IdView
    val instructionId: String

    @Column(name = "clause_kind")
    val clauseKind: String

    @Column(name = "clause_text")
    val clauseText: String

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}

