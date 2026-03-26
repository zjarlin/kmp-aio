package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.config.PlaygroundJdbcTransactionContext
import site.addzero.coding.playground.server.domain.PlaygroundNotFoundException
import site.addzero.coding.playground.server.entity.*
import java.sql.Connection
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

@Single
class MetadataPersistenceSupport(
    val sqlClient: KSqlClient,
    private val dataSource: DataSource,
) {
    fun newId(): String = UUID.randomUUID().toString()

    fun now(): LocalDateTime = LocalDateTime.now()

    fun moduleRef(id: String): LlvmModule = new(LlvmModule::class).by { this.id = id }
    fun typeRef(id: String): LlvmType = new(LlvmType::class).by { this.id = id }
    fun comdatRef(id: String): LlvmComdat = new(LlvmComdat::class).by { this.id = id }
    fun attributeGroupRef(id: String): LlvmAttributeGroup = new(LlvmAttributeGroup::class).by { this.id = id }
    fun constantRef(id: String): LlvmConstant = new(LlvmConstant::class).by { this.id = id }
    fun inlineAsmRef(id: String): LlvmInlineAsm = new(LlvmInlineAsm::class).by { this.id = id }
    fun globalRef(id: String): LlvmGlobalVariable = new(LlvmGlobalVariable::class).by { this.id = id }
    fun functionRef(id: String): LlvmFunction = new(LlvmFunction::class).by { this.id = id }
    fun paramRef(id: String): LlvmFunctionParam = new(LlvmFunctionParam::class).by { this.id = id }
    fun blockRef(id: String): LlvmBasicBlock = new(LlvmBasicBlock::class).by { this.id = id }
    fun instructionRef(id: String): LlvmInstruction = new(LlvmInstruction::class).by { this.id = id }
    fun operandRef(id: String): LlvmOperand = new(LlvmOperand::class).by { this.id = id }
    fun metadataNodeRef(id: String): LlvmMetadataNode = new(LlvmMetadataNode::class).by { this.id = id }
    fun namedMetadataRef(id: String): LlvmNamedMetadata = new(LlvmNamedMetadata::class).by { this.id = id }
    fun compileProfileRef(id: String): LlvmCompileProfile = new(LlvmCompileProfile::class).by { this.id = id }
    fun compileJobRef(id: String): LlvmCompileJob = new(LlvmCompileJob::class).by { this.id = id }

    fun <T> inTransaction(block: () -> T): T = PlaygroundJdbcTransactionContext.withTransaction(dataSource, block)

    fun <T> withJdbcConnection(block: (Connection) -> T): T {
        val current = PlaygroundJdbcTransactionContext.connectionOrNull()
        return if (current != null) {
            block(current)
        } else {
            dataSource.connection.use(block)
        }
    }

    fun moduleOrThrow(id: String): LlvmModule =
        sqlClient.findById(LlvmModule::class, id) ?: throw PlaygroundNotFoundException("LLVM module '$id' not found")

    fun typeOrThrow(id: String): LlvmType =
        sqlClient.findById(LlvmType::class, id) ?: throw PlaygroundNotFoundException("LLVM type '$id' not found")

    fun typeMemberOrThrow(id: String): LlvmTypeMember =
        sqlClient.findById(LlvmTypeMember::class, id) ?: throw PlaygroundNotFoundException("LLVM type member '$id' not found")

    fun comdatOrThrow(id: String): LlvmComdat =
        sqlClient.findById(LlvmComdat::class, id) ?: throw PlaygroundNotFoundException("LLVM comdat '$id' not found")

    fun attributeGroupOrThrow(id: String): LlvmAttributeGroup =
        sqlClient.findById(LlvmAttributeGroup::class, id) ?: throw PlaygroundNotFoundException("LLVM attribute group '$id' not found")

    fun attributeEntryOrThrow(id: String): LlvmAttributeEntry =
        sqlClient.findById(LlvmAttributeEntry::class, id) ?: throw PlaygroundNotFoundException("LLVM attribute entry '$id' not found")

    fun globalOrThrow(id: String): LlvmGlobalVariable =
        sqlClient.findById(LlvmGlobalVariable::class, id) ?: throw PlaygroundNotFoundException("LLVM global '$id' not found")

    fun aliasOrThrow(id: String): LlvmAlias =
        sqlClient.findById(LlvmAlias::class, id) ?: throw PlaygroundNotFoundException("LLVM alias '$id' not found")

    fun ifuncOrThrow(id: String): LlvmIfunc =
        sqlClient.findById(LlvmIfunc::class, id) ?: throw PlaygroundNotFoundException("LLVM ifunc '$id' not found")

    fun inlineAsmOrThrow(id: String): LlvmInlineAsm =
        sqlClient.findById(LlvmInlineAsm::class, id) ?: throw PlaygroundNotFoundException("LLVM inline asm '$id' not found")

    fun constantOrThrow(id: String): LlvmConstant =
        sqlClient.findById(LlvmConstant::class, id) ?: throw PlaygroundNotFoundException("LLVM constant '$id' not found")

    fun constantItemOrThrow(id: String): LlvmConstantItem =
        sqlClient.findById(LlvmConstantItem::class, id) ?: throw PlaygroundNotFoundException("LLVM constant item '$id' not found")

    fun functionOrThrow(id: String): LlvmFunction =
        sqlClient.findById(LlvmFunction::class, id) ?: throw PlaygroundNotFoundException("LLVM function '$id' not found")

    fun paramOrThrow(id: String): LlvmFunctionParam =
        sqlClient.findById(LlvmFunctionParam::class, id) ?: throw PlaygroundNotFoundException("LLVM function param '$id' not found")

    fun blockOrThrow(id: String): LlvmBasicBlock =
        sqlClient.findById(LlvmBasicBlock::class, id) ?: throw PlaygroundNotFoundException("LLVM basic block '$id' not found")

    fun instructionOrThrow(id: String): LlvmInstruction =
        sqlClient.findById(LlvmInstruction::class, id) ?: throw PlaygroundNotFoundException("LLVM instruction '$id' not found")

    fun operandOrThrow(id: String): LlvmOperand =
        sqlClient.findById(LlvmOperand::class, id) ?: throw PlaygroundNotFoundException("LLVM operand '$id' not found")

    fun phiIncomingOrThrow(id: String): LlvmPhiIncoming =
        sqlClient.findById(LlvmPhiIncoming::class, id) ?: throw PlaygroundNotFoundException("LLVM phi incoming '$id' not found")

    fun clauseOrThrow(id: String): LlvmInstructionClause =
        sqlClient.findById(LlvmInstructionClause::class, id) ?: throw PlaygroundNotFoundException("LLVM instruction clause '$id' not found")

    fun bundleOrThrow(id: String): LlvmOperandBundle =
        sqlClient.findById(LlvmOperandBundle::class, id) ?: throw PlaygroundNotFoundException("LLVM operand bundle '$id' not found")

    fun namedMetadataOrThrow(id: String): LlvmNamedMetadata =
        sqlClient.findById(LlvmNamedMetadata::class, id) ?: throw PlaygroundNotFoundException("LLVM named metadata '$id' not found")

    fun metadataNodeOrThrow(id: String): LlvmMetadataNode =
        sqlClient.findById(LlvmMetadataNode::class, id) ?: throw PlaygroundNotFoundException("LLVM metadata node '$id' not found")

    fun metadataFieldOrThrow(id: String): LlvmMetadataField =
        sqlClient.findById(LlvmMetadataField::class, id) ?: throw PlaygroundNotFoundException("LLVM metadata field '$id' not found")

    fun metadataAttachmentOrThrow(id: String): LlvmMetadataAttachment =
        sqlClient.findById(LlvmMetadataAttachment::class, id) ?: throw PlaygroundNotFoundException("LLVM metadata attachment '$id' not found")

    fun compileProfileOrThrow(id: String): LlvmCompileProfile =
        sqlClient.findById(LlvmCompileProfile::class, id) ?: throw PlaygroundNotFoundException("LLVM compile profile '$id' not found")

    fun compileJobOrThrow(id: String): LlvmCompileJob =
        sqlClient.findById(LlvmCompileJob::class, id) ?: throw PlaygroundNotFoundException("LLVM compile job '$id' not found")

    fun compileArtifactOrThrow(id: String): LlvmCompileArtifact =
        sqlClient.findById(LlvmCompileArtifact::class, id) ?: throw PlaygroundNotFoundException("LLVM compile artifact '$id' not found")

    fun listModules(): List<LlvmModule> = sqlClient.createQuery(LlvmModule::class) { select(table) }
        .execute()
        .sortedBy { it.name.lowercase() }

    fun listTypes(moduleId: String? = null): List<LlvmType> = sqlClient.createQuery(LlvmType::class) { select(table) }
        .execute()
        .filter { moduleId == null || it.moduleId == moduleId }
        .sortedBy { it.orderIndex }

    fun listTypeMembers(typeId: String? = null): List<LlvmTypeMember> =
        sqlClient.createQuery(LlvmTypeMember::class) { select(table) }
            .execute()
            .filter { typeId == null || it.typeId == typeId }
            .sortedBy { it.orderIndex }

    fun listComdats(moduleId: String? = null): List<LlvmComdat> = sqlClient.createQuery(LlvmComdat::class) { select(table) }
        .execute()
        .filter { moduleId == null || it.moduleId == moduleId }
        .sortedBy { it.orderIndex }

    fun listAttributeGroups(moduleId: String? = null): List<LlvmAttributeGroup> =
        sqlClient.createQuery(LlvmAttributeGroup::class) { select(table) }
            .execute()
            .filter { moduleId == null || it.moduleId == moduleId }
            .sortedBy { it.orderIndex }

    fun listAttributeEntries(groupId: String? = null): List<LlvmAttributeEntry> =
        sqlClient.createQuery(LlvmAttributeEntry::class) { select(table) }
            .execute()
            .filter { groupId == null || it.attributeGroupId == groupId }
            .sortedBy { it.orderIndex }

    fun listGlobals(moduleId: String? = null): List<LlvmGlobalVariable> =
        sqlClient.createQuery(LlvmGlobalVariable::class) { select(table) }
            .execute()
            .filter { moduleId == null || it.moduleId == moduleId }
            .sortedBy { it.orderIndex }

    fun listAliases(moduleId: String? = null): List<LlvmAlias> = sqlClient.createQuery(LlvmAlias::class) { select(table) }
        .execute()
        .filter { moduleId == null || it.moduleId == moduleId }
        .sortedBy { it.orderIndex }

    fun listIfuncs(moduleId: String? = null): List<LlvmIfunc> = sqlClient.createQuery(LlvmIfunc::class) { select(table) }
        .execute()
        .filter { moduleId == null || it.moduleId == moduleId }
        .sortedBy { it.orderIndex }

    fun listInlineAsms(moduleId: String? = null): List<LlvmInlineAsm> =
        sqlClient.createQuery(LlvmInlineAsm::class) { select(table) }
            .execute()
            .filter { moduleId == null || it.moduleId == moduleId }
            .sortedBy { it.orderIndex }

    fun listConstants(moduleId: String? = null): List<LlvmConstant> =
        sqlClient.createQuery(LlvmConstant::class) { select(table) }
            .execute()
            .filter { moduleId == null || it.moduleId == moduleId }
            .sortedBy { it.orderIndex }

    fun listConstantItems(constantId: String? = null): List<LlvmConstantItem> =
        sqlClient.createQuery(LlvmConstantItem::class) { select(table) }
            .execute()
            .filter { constantId == null || it.constantId == constantId }
            .sortedBy { it.orderIndex }

    fun listFunctions(moduleId: String? = null): List<LlvmFunction> =
        sqlClient.createQuery(LlvmFunction::class) { select(table) }
            .execute()
            .filter { moduleId == null || it.moduleId == moduleId }
            .sortedBy { it.orderIndex }

    fun listParams(functionId: String? = null): List<LlvmFunctionParam> =
        sqlClient.createQuery(LlvmFunctionParam::class) { select(table) }
            .execute()
            .filter { functionId == null || it.functionId == functionId }
            .sortedBy { it.orderIndex }

    fun listBlocks(functionId: String? = null): List<LlvmBasicBlock> =
        sqlClient.createQuery(LlvmBasicBlock::class) { select(table) }
            .execute()
            .filter { functionId == null || it.functionId == functionId }
            .sortedBy { it.orderIndex }

    fun listInstructions(blockId: String? = null): List<LlvmInstruction> =
        sqlClient.createQuery(LlvmInstruction::class) { select(table) }
            .execute()
            .filter { blockId == null || it.blockId == blockId }
            .sortedBy { it.orderIndex }

    fun listOperands(instructionId: String? = null): List<LlvmOperand> =
        sqlClient.createQuery(LlvmOperand::class) { select(table) }
            .execute()
            .filter { instructionId == null || it.instructionId == instructionId }
            .sortedBy { it.orderIndex }

    fun listPhiIncoming(instructionId: String? = null): List<LlvmPhiIncoming> =
        sqlClient.createQuery(LlvmPhiIncoming::class) { select(table) }
            .execute()
            .filter { instructionId == null || it.instructionId == instructionId }
            .sortedBy { it.orderIndex }

    fun listClauses(instructionId: String? = null): List<LlvmInstructionClause> =
        sqlClient.createQuery(LlvmInstructionClause::class) { select(table) }
            .execute()
            .filter { instructionId == null || it.instructionId == instructionId }
            .sortedBy { it.orderIndex }

    fun listBundles(instructionId: String? = null): List<LlvmOperandBundle> =
        sqlClient.createQuery(LlvmOperandBundle::class) { select(table) }
            .execute()
            .filter { instructionId == null || it.instructionId == instructionId }
            .sortedBy { it.orderIndex }

    fun listNamedMetadata(moduleId: String? = null): List<LlvmNamedMetadata> =
        sqlClient.createQuery(LlvmNamedMetadata::class) { select(table) }
            .execute()
            .filter { moduleId == null || it.moduleId == moduleId }
            .sortedBy { it.orderIndex }

    fun listMetadataNodes(moduleId: String? = null): List<LlvmMetadataNode> =
        sqlClient.createQuery(LlvmMetadataNode::class) { select(table) }
            .execute()
            .filter { moduleId == null || it.moduleId == moduleId }
            .sortedBy { it.orderIndex }

    fun listMetadataFields(metadataNodeId: String? = null, namedMetadataId: String? = null): List<LlvmMetadataField> =
        sqlClient.createQuery(LlvmMetadataField::class) { select(table) }
            .execute()
            .filter { metadataNodeId == null || it.metadataNodeId == metadataNodeId }
            .filter { namedMetadataId == null || it.namedMetadataId == namedMetadataId }
            .sortedBy { it.orderIndex }

    fun listMetadataAttachments(moduleId: String? = null): List<LlvmMetadataAttachment> =
        sqlClient.createQuery(LlvmMetadataAttachment::class) { select(table) }
            .execute()
            .filter { attachment ->
                if (moduleId == null) {
                    true
                } else {
                    val functionModule = attachment.functionId?.let(::functionOrThrow)?.moduleId
                    val globalModule = attachment.globalVariableId?.let(::globalOrThrow)?.moduleId
                    val instructionModule = attachment.instructionId?.let(::instructionOrThrow)?.blockId?.let(::blockOrThrow)?.functionId?.let(::functionOrThrow)?.moduleId
                    moduleId == functionModule || moduleId == globalModule || moduleId == instructionModule
                }
            }
            .sortedBy { it.orderIndex }

    fun listCompileProfiles(moduleId: String? = null): List<LlvmCompileProfile> =
        sqlClient.createQuery(LlvmCompileProfile::class) { select(table) }
            .execute()
            .filter { moduleId == null || it.moduleId == moduleId }
            .sortedBy { it.orderIndex }

    fun listCompileJobs(moduleId: String? = null): List<LlvmCompileJob> =
        sqlClient.createQuery(LlvmCompileJob::class) { select(table) }
            .execute()
            .filter { moduleId == null || it.moduleId == moduleId }
            .sortedByDescending { it.createdAt }

    fun listCompileArtifacts(jobId: String? = null): List<LlvmCompileArtifact> =
        sqlClient.createQuery(LlvmCompileArtifact::class) { select(table) }
            .execute()
            .filter { jobId == null || it.jobId == jobId }

    fun nextOrder(existing: List<*>): Int = existing.size
}
