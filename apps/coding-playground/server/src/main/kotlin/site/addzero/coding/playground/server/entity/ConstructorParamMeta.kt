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
@Table(name = "constructor_param_meta")
interface ConstructorParamMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "declaration_id")
    val declaration: DeclarationMeta

    @IdView
    val declarationId: String

    val name: String
    val type: String
    val mutable: Boolean
    val nullable: Boolean

    @Column(name = "default_value")
    val defaultValue: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
