package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "declaration_meta")
interface DeclarationMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "file_id")
    val file: SourceFileMeta

    @IdView
    val fileId: String

    @Column(name = "target_id")
    val targetId: String

    @Column(name = "package_name")
    val packageName: String

    @Column(name = "fq_name")
    val fqName: String

    val name: String
    val kind: String
    val visibility: String

    @Column(name = "modifiers_json")
    val modifiersJson: String?

    @Column(name = "super_types_json")
    val superTypesJson: String?

    @Column(name = "doc_comment")
    val docComment: String?

    @Column(name = "order_index")
    val orderIndex: Int

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "declaration")
    val constructorParams: List<ConstructorParamMeta>

    @OneToMany(mappedBy = "declaration")
    val properties: List<PropertyMeta>

    @OneToMany(mappedBy = "declaration")
    val enumEntries: List<EnumEntryMeta>

    @OneToMany(mappedBy = "declaration")
    val functionStubs: List<FunctionStubMeta>
}
