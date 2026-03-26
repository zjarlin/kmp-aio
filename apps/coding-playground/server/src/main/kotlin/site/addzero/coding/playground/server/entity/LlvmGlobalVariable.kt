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
@Table(name = "llvm_global_variable")
interface LlvmGlobalVariable {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "module_id")
    val module: LlvmModule

    @IdView
    val moduleId: String

    val name: String
    val symbol: String

    @Column(name = "type_text")
    val typeText: String

    @ManyToOne
    @JoinColumn(name = "type_ref_id")
    val typeRef: LlvmType?

    @IdView("typeRef")
    val typeRefId: String?

    val linkage: String
    val visibility: String
    val constant: Boolean

    @Column(name = "thread_local")
    val threadLocal: Boolean

    @Column(name = "externally_initialized")
    val externallyInitialized: Boolean

    @Column(name = "initializer_text")
    val initializerText: String?

    @ManyToOne
    @JoinColumn(name = "initializer_constant_id")
    val initializerConstant: LlvmConstant?

    @IdView("initializerConstant")
    val initializerConstantId: String?

    @Column(name = "section_name")
    val sectionName: String?

    @ManyToOne
    @JoinColumn(name = "comdat_id")
    val comdat: LlvmComdat?

    @IdView("comdat")
    val comdatId: String?

    val alignment: Int?

    @Column(name = "address_space")
    val addressSpace: Int?

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
}

