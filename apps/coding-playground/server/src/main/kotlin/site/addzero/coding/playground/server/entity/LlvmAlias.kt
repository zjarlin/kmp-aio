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
@Table(name = "llvm_alias")
interface LlvmAlias {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "module_id")
    val module: LlvmModule

    @IdView
    val moduleId: String

    val name: String
    val symbol: String

    @Column(name = "aliasee_text")
    val aliaseeText: String

    @ManyToOne
    @JoinColumn(name = "aliasee_global_id")
    val aliaseeGlobal: LlvmGlobalVariable?

    @IdView("aliaseeGlobal")
    val aliaseeGlobalId: String?

    val linkage: String
    val visibility: String

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}

