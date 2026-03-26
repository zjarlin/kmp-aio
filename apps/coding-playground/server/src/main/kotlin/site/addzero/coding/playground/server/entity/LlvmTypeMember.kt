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
@Table(name = "llvm_type_member")
interface LlvmTypeMember {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "type_id")
    val type: LlvmType

    @IdView
    val typeId: String

    val name: String

    @Column(name = "member_type_text")
    val memberTypeText: String

    @ManyToOne
    @JoinColumn(name = "member_type_ref_id")
    val memberTypeRef: LlvmType?

    @IdView("memberTypeRef")
    val memberTypeRefId: String?

    @Column(name = "metadata_json")
    val metadataJson: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}

