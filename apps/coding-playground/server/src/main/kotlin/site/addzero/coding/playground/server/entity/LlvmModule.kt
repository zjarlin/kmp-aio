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
@Table(name = "llvm_module")
interface LlvmModule {
    @Id
    val id: String

    val name: String

    @Column(name = "source_filename")
    val sourceFilename: String

    @Column(name = "target_triple")
    val targetTriple: String

    @Column(name = "data_layout")
    val dataLayout: String

    @Column(name = "module_asm")
    val moduleAsm: String?

    @Column(name = "module_flags_json")
    val moduleFlagsJson: String?

    val description: String?

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "module")
    val types: List<LlvmType>

    @OneToMany(mappedBy = "module")
    val globals: List<LlvmGlobalVariable>

    @OneToMany(mappedBy = "module")
    val aliases: List<LlvmAlias>

    @OneToMany(mappedBy = "module")
    val ifuncs: List<LlvmIfunc>

    @OneToMany(mappedBy = "module")
    val comdats: List<LlvmComdat>

    @OneToMany(mappedBy = "module")
    val attributeGroups: List<LlvmAttributeGroup>

    @OneToMany(mappedBy = "module")
    val constants: List<LlvmConstant>

    @OneToMany(mappedBy = "module")
    val inlineAsms: List<LlvmInlineAsm>

    @OneToMany(mappedBy = "module")
    val functions: List<LlvmFunction>

    @OneToMany(mappedBy = "module")
    val namedMetadata: List<LlvmNamedMetadata>

    @OneToMany(mappedBy = "module")
    val metadataNodes: List<LlvmMetadataNode>

    @OneToMany(mappedBy = "module")
    val compileProfiles: List<LlvmCompileProfile>

    @OneToMany(mappedBy = "module")
    val compileJobs: List<LlvmCompileJob>
}

