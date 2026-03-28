package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "function_stub_meta")
interface FunctionStubMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "declaration_id")
    val declaration: DeclarationMeta

    @IdView
    val declarationId: String

    val name: String

    @Column(name = "return_type")
    val returnType: String

    val visibility: String

    @Column(name = "modifiers_json")
    val modifiersJson: String?

    @Column(name = "parameters_json")
    val parametersJson: String?

    @Column(name = "body_mode")
    val bodyMode: String

    @Column(name = "body_text")
    val bodyText: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
