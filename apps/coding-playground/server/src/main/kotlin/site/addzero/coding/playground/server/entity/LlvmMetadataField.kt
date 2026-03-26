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
@Table(name = "llvm_metadata_field")
interface LlvmMetadataField {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "metadata_node_id")
    val metadataNode: LlvmMetadataNode?

    @IdView("metadataNode")
    val metadataNodeId: String?

    @ManyToOne
    @JoinColumn(name = "named_metadata_id")
    val namedMetadata: LlvmNamedMetadata?

    @IdView("namedMetadata")
    val namedMetadataId: String?

    @Column(name = "value_kind")
    val valueKind: String

    @Column(name = "value_text")
    val valueText: String

    @ManyToOne
    @JoinColumn(name = "referenced_node_id")
    val referencedNode: LlvmMetadataNode?

    @IdView("referencedNode")
    val referencedNodeId: String?

    @ManyToOne
    @JoinColumn(name = "referenced_constant_id")
    val referencedConstant: LlvmConstant?

    @IdView("referencedConstant")
    val referencedConstantId: String?

    @ManyToOne
    @JoinColumn(name = "referenced_type_id")
    val referencedType: LlvmType?

    @IdView("referencedType")
    val referencedTypeId: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}

