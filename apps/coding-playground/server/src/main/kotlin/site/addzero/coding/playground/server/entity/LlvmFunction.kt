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
@Table(name = "llvm_function")
interface LlvmFunction {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "module_id")
    val module: LlvmModule

    @IdView
    val moduleId: String

    val name: String
    val symbol: String

    @Column(name = "return_type_text")
    val returnTypeText: String

    @ManyToOne
    @JoinColumn(name = "return_type_ref_id")
    val returnTypeRef: LlvmType?

    @IdView("returnTypeRef")
    val returnTypeRefId: String?

    val linkage: String
    val visibility: String

    @Column(name = "calling_convention")
    val callingConvention: String

    val variadic: Boolean

    @Column(name = "declaration_only")
    val declarationOnly: Boolean

    @Column(name = "gc_name")
    val gcName: String?

    @Column(name = "personality_text")
    val personalityText: String?

    @ManyToOne
    @JoinColumn(name = "comdat_id")
    val comdat: LlvmComdat?

    @IdView("comdat")
    val comdatId: String?

    @Column(name = "section_name")
    val sectionName: String?

    @Column(name = "attribute_group_ids_json")
    val attributeGroupIdsJson: String?

    @Column(name = "metadata_json")
    val metadataJson: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "function")
    val params: List<LlvmFunctionParam>

    @OneToMany(mappedBy = "function")
    val blocks: List<LlvmBasicBlock>
}

