package site.addzero.coding.playground

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.*
import java.util.prefs.Preferences

enum class PlaygroundUiLanguage {
    ZH_CN,
    EN_US,
}

@Single
class PlaygroundWorkbenchState(
    private val moduleService: LlvmModuleService,
    private val typeService: LlvmTypeService,
    private val globalValueService: LlvmGlobalValueService,
    private val functionService: LlvmFunctionService,
    private val metadataService: LlvmMetadataService,
    private val attributeService: LlvmAttributeService,
    private val validationService: LlvmValidationService,
    private val snapshotService: LlvmSnapshotService,
    private val exportService: LlvmLlExportService,
    private val compileProfileService: LlvmCompileProfileService,
    private val compileJobService: LlvmCompileJobService,
) {
    private val prefs = Preferences.userNodeForPackage(PlaygroundWorkbenchState::class.java)
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    var modules by mutableStateOf<List<LlvmModuleDto>>(emptyList())
        private set
    var types by mutableStateOf<List<LlvmTypeDto>>(emptyList())
        private set
    var typeMembers by mutableStateOf<List<LlvmTypeMemberDto>>(emptyList())
        private set
    var globals by mutableStateOf<List<LlvmGlobalVariableDto>>(emptyList())
        private set
    var constants by mutableStateOf<List<LlvmConstantDto>>(emptyList())
        private set
    var functions by mutableStateOf<List<LlvmFunctionDto>>(emptyList())
        private set
    var params by mutableStateOf<List<LlvmFunctionParamDto>>(emptyList())
        private set
    var blocks by mutableStateOf<List<LlvmBasicBlockDto>>(emptyList())
        private set
    var instructions by mutableStateOf<List<LlvmInstructionDto>>(emptyList())
        private set
    var operands by mutableStateOf<List<LlvmOperandDto>>(emptyList())
        private set
    var namedMetadata by mutableStateOf<List<LlvmNamedMetadataDto>>(emptyList())
        private set
    var metadataNodes by mutableStateOf<List<LlvmMetadataNodeDto>>(emptyList())
        private set
    var metadataFields by mutableStateOf<List<LlvmMetadataFieldDto>>(emptyList())
        private set
    var metadataAttachments by mutableStateOf<List<LlvmMetadataAttachmentDto>>(emptyList())
        private set
    var attributeGroups by mutableStateOf<List<LlvmAttributeGroupDto>>(emptyList())
        private set
    var attributeEntries by mutableStateOf<List<LlvmAttributeEntryDto>>(emptyList())
        private set
    var compileProfiles by mutableStateOf<List<LlvmCompileProfileDto>>(emptyList())
        private set
    var compileJobs by mutableStateOf<List<LlvmCompileJobDto>>(emptyList())
        private set
    var compileArtifacts by mutableStateOf<List<LlvmCompileArtifactDto>>(emptyList())
        private set

    var selectedModuleId by mutableStateOf<String?>(null)
        private set
    var selectedTypeId by mutableStateOf<String?>(null)
        private set
    var selectedFunctionId by mutableStateOf<String?>(null)
        private set
    var selectedBlockId by mutableStateOf<String?>(null)
        private set
    var selectedInstructionId by mutableStateOf<String?>(null)
        private set
    var selectedMetadataNodeId by mutableStateOf<String?>(null)
        private set
    var selectedNamedMetadataId by mutableStateOf<String?>(null)
        private set
    var selectedProfileId by mutableStateOf<String?>(null)
        private set
    var selectedJobId by mutableStateOf<String?>(null)
        private set

    var uiLanguage by mutableStateOf(loadLanguage())
        private set
    var searchQuery by mutableStateOf("")
    var statusMessage by mutableStateOf("LLVM IR 工作台已就绪")
        private set
    var validationIssues by mutableStateOf<List<LlvmValidationIssueDto>>(emptyList())
        private set
    var exportPreviewText by mutableStateOf("")
        private set
    var snapshotEditorText by mutableStateOf("")
    var lastDeleteCheck by mutableStateOf<LlvmDeleteCheckResultDto?>(null)
        private set
    var lastCompileResult by mutableStateOf<LlvmCompileExecutionResultDto?>(null)
        private set

    suspend fun refreshAll() {
        modules = moduleService.list(LlvmSearchRequest(query = searchQuery.ifBlank { null }))
        if (selectedModuleId !in modules.map { it.id }) {
            selectedModuleId = modules.firstOrNull()?.id
        }
        refreshModuleScope()
    }

    suspend fun refreshModuleScope() {
        val moduleId = selectedModuleId
        if (moduleId == null) {
            types = emptyList()
            typeMembers = emptyList()
            globals = emptyList()
            constants = emptyList()
            functions = emptyList()
            params = emptyList()
            blocks = emptyList()
            instructions = emptyList()
            operands = emptyList()
            namedMetadata = emptyList()
            metadataNodes = emptyList()
            metadataFields = emptyList()
            metadataAttachments = emptyList()
            attributeGroups = emptyList()
            attributeEntries = emptyList()
            compileProfiles = emptyList()
            compileJobs = emptyList()
            compileArtifacts = emptyList()
            return
        }
        val aggregate = moduleService.aggregate(moduleId)
        types = aggregate.types
        typeMembers = aggregate.typeMembers
        globals = aggregate.globals
        constants = aggregate.constants
        functions = aggregate.functions
        params = aggregate.params
        blocks = aggregate.blocks
        instructions = aggregate.instructions
        operands = aggregate.operands
        namedMetadata = aggregate.namedMetadata
        metadataNodes = aggregate.metadataNodes
        metadataFields = aggregate.metadataFields
        metadataAttachments = aggregate.metadataAttachments
        attributeGroups = aggregate.attributeGroups
        attributeEntries = aggregate.attributeEntries
        compileProfiles = aggregate.compileProfiles
        compileJobs = aggregate.compileJobs
        compileArtifacts = aggregate.compileArtifacts
        if (selectedTypeId !in types.map { it.id }) selectedTypeId = types.firstOrNull()?.id
        if (selectedFunctionId !in functions.map { it.id }) selectedFunctionId = functions.firstOrNull()?.id
        if (selectedBlockId !in blocks.map { it.id }) selectedBlockId = blocks.firstOrNull { it.functionId == selectedFunctionId }?.id
        if (selectedInstructionId !in instructions.map { it.id }) selectedInstructionId = instructions.firstOrNull { it.blockId == selectedBlockId }?.id
        if (selectedMetadataNodeId !in metadataNodes.map { it.id }) selectedMetadataNodeId = metadataNodes.firstOrNull()?.id
        if (selectedNamedMetadataId !in namedMetadata.map { it.id }) selectedNamedMetadataId = namedMetadata.firstOrNull()?.id
        if (selectedProfileId !in compileProfiles.map { it.id }) selectedProfileId = compileProfiles.firstOrNull()?.id
        if (selectedJobId !in compileJobs.map { it.id }) selectedJobId = compileJobs.firstOrNull()?.id
    }

    fun selectModule(id: String?) {
        selectedModuleId = id
        clearDiagnostics()
    }

    fun selectType(id: String?) {
        selectedTypeId = id
        clearDiagnostics()
    }

    fun selectFunction(id: String?) {
        selectedFunctionId = id
        selectedBlockId = blocks.firstOrNull { it.functionId == id }?.id
        selectedInstructionId = instructions.firstOrNull { it.blockId == selectedBlockId }?.id
        clearDiagnostics()
    }

    fun selectBlock(id: String?) {
        selectedBlockId = id
        selectedInstructionId = instructions.firstOrNull { it.blockId == id }?.id
        clearDiagnostics()
    }

    fun selectInstruction(id: String?) {
        selectedInstructionId = id
        clearDiagnostics()
    }

    fun selectMetadataNode(id: String?) {
        selectedMetadataNodeId = id
        clearDiagnostics()
    }

    fun selectNamedMetadata(id: String?) {
        selectedNamedMetadataId = id
        clearDiagnostics()
    }

    fun selectProfile(id: String?) {
        selectedProfileId = id
        clearDiagnostics()
    }

    fun selectJob(id: String?) {
        selectedJobId = id
        compileArtifacts = if (id == null) emptyList() else compileArtifacts.filter { it.jobId == id }
        clearDiagnostics()
    }

    fun toggleLanguage() {
        uiLanguage = when (uiLanguage) {
            PlaygroundUiLanguage.ZH_CN -> PlaygroundUiLanguage.EN_US
            PlaygroundUiLanguage.EN_US -> PlaygroundUiLanguage.ZH_CN
        }
        prefs.put("language", uiLanguage.name)
    }

    suspend fun saveModule(selectedId: String?, request: CreateLlvmModuleRequest) {
        val result = if (selectedId == null) {
            moduleService.create(request)
        } else {
            moduleService.update(
                selectedId,
                UpdateLlvmModuleRequest(
                    name = request.name,
                    sourceFilename = request.sourceFilename,
                    targetTriple = request.targetTriple,
                    dataLayout = request.dataLayout,
                    moduleAsm = request.moduleAsm,
                    moduleFlags = request.moduleFlags,
                    description = request.description,
                ),
            )
        }
        selectedModuleId = result.id
        statusMessage = "模块已保存"
        refreshAll()
    }

    suspend fun removeSelectedModule() {
        selectedModuleId?.let {
            moduleService.delete(it)
            statusMessage = "模块已删除"
            refreshAll()
        }
    }

    suspend fun saveType(selectedId: String?, request: CreateLlvmTypeRequest) {
        val moduleId = selectedModuleId ?: return
        val created = if (selectedId == null) {
            typeService.create(request.copy(moduleId = moduleId))
        } else {
            typeService.update(
                selectedId,
                UpdateLlvmTypeRequest(
                    name = request.name,
                    symbol = request.symbol,
                    kind = request.kind,
                    primitiveWidth = request.primitiveWidth,
                    packed = request.packed,
                    opaque = request.opaque,
                    addressSpace = request.addressSpace,
                    arrayLength = request.arrayLength,
                    scalable = request.scalable,
                    variadic = request.variadic,
                    definitionText = request.definitionText,
                    elementTypeRefId = request.elementTypeRefId,
                    returnTypeRefId = request.returnTypeRefId,
                ),
            )
        }
        selectedTypeId = created.id
        statusMessage = "类型已保存"
        refreshModuleScope()
    }

    suspend fun removeSelectedType() {
        selectedTypeId?.let {
            typeService.delete(it)
            statusMessage = "类型已删除"
            refreshModuleScope()
        }
    }

    suspend fun saveGlobal(selectedId: String?, request: CreateLlvmGlobalVariableRequest) {
        val moduleId = selectedModuleId ?: return
        if (selectedId == null) {
            globalValueService.createGlobal(request.copy(moduleId = moduleId))
        } else {
            globalValueService.updateGlobal(
                selectedId,
                UpdateLlvmGlobalVariableRequest(
                    name = request.name,
                    symbol = request.symbol,
                    typeText = request.typeText,
                    typeRefId = request.typeRefId,
                    linkage = request.linkage,
                    visibility = request.visibility,
                    constant = request.constant,
                    threadLocal = request.threadLocal,
                    externallyInitialized = request.externallyInitialized,
                    initializerText = request.initializerText,
                    initializerConstantId = request.initializerConstantId,
                    sectionName = request.sectionName,
                    comdatId = request.comdatId,
                    alignment = request.alignment,
                    addressSpace = request.addressSpace,
                    attributeGroupIds = request.attributeGroupIds,
                    metadata = request.metadata,
                ),
            )
        }
        statusMessage = "全局变量已保存"
        refreshModuleScope()
    }

    suspend fun removeGlobal(id: String) {
        globalValueService.deleteGlobal(id)
        statusMessage = "全局变量已删除"
        refreshModuleScope()
    }

    suspend fun saveFunction(selectedId: String?, request: CreateLlvmFunctionRequest) {
        val moduleId = selectedModuleId ?: return
        val result = if (selectedId == null) {
            functionService.create(request.copy(moduleId = moduleId))
        } else {
            functionService.update(
                selectedId,
                UpdateLlvmFunctionRequest(
                    name = request.name,
                    symbol = request.symbol,
                    returnTypeText = request.returnTypeText,
                    returnTypeRefId = request.returnTypeRefId,
                    linkage = request.linkage,
                    visibility = request.visibility,
                    callingConvention = request.callingConvention,
                    variadic = request.variadic,
                    declarationOnly = request.declarationOnly,
                    gcName = request.gcName,
                    personalityText = request.personalityText,
                    comdatId = request.comdatId,
                    sectionName = request.sectionName,
                    attributeGroupIds = request.attributeGroupIds,
                    metadata = request.metadata,
                ),
            )
        }
        selectedFunctionId = result.id
        statusMessage = "函数已保存"
        refreshModuleScope()
    }

    suspend fun removeSelectedFunction() {
        selectedFunctionId?.let {
            functionService.delete(it)
            statusMessage = "函数已删除"
            refreshModuleScope()
        }
    }

    suspend fun saveBlock(selectedId: String?, name: String, label: String) {
        val functionId = selectedFunctionId ?: return
        val result = if (selectedId == null) {
            functionService.createBlock(CreateLlvmBasicBlockRequest(functionId = functionId, name = name, label = label))
        } else {
            functionService.updateBlock(selectedId, UpdateLlvmBasicBlockRequest(name = name, label = label))
        }
        selectedBlockId = result.id
        statusMessage = "基本块已保存"
        refreshModuleScope()
    }

    suspend fun removeSelectedBlock() {
        selectedBlockId?.let {
            functionService.deleteBlock(it)
            statusMessage = "基本块已删除"
            refreshModuleScope()
        }
    }

    suspend fun saveInstruction(selectedId: String?, request: CreateLlvmInstructionRequest) {
        val blockId = selectedBlockId ?: return
        val result = if (selectedId == null) {
            functionService.createInstruction(request.copy(blockId = blockId))
        } else {
            functionService.updateInstruction(
                selectedId,
                UpdateLlvmInstructionRequest(
                    opcode = request.opcode,
                    resultSymbol = request.resultSymbol,
                    typeText = request.typeText,
                    typeRefId = request.typeRefId,
                    textSuffix = request.textSuffix,
                    flags = request.flags,
                    terminator = request.terminator,
                ),
            )
        }
        selectedInstructionId = result.id
        statusMessage = "指令已保存"
        refreshModuleScope()
    }

    suspend fun removeSelectedInstruction() {
        selectedInstructionId?.let {
            functionService.deleteInstruction(it)
            statusMessage = "指令已删除"
            refreshModuleScope()
        }
    }

    suspend fun saveOperand(selectedId: String?, request: CreateLlvmOperandRequest) {
        val instructionId = selectedInstructionId ?: return
        if (selectedId == null) {
            functionService.createOperand(request.copy(instructionId = instructionId))
        } else {
            functionService.updateOperand(
                selectedId,
                UpdateLlvmOperandRequest(
                    kind = request.kind,
                    text = request.text,
                    referencedInstructionId = request.referencedInstructionId,
                    referencedFunctionId = request.referencedFunctionId,
                    referencedParamId = request.referencedParamId,
                    referencedGlobalId = request.referencedGlobalId,
                    referencedConstantId = request.referencedConstantId,
                    referencedBlockId = request.referencedBlockId,
                    referencedMetadataNodeId = request.referencedMetadataNodeId,
                    referencedTypeId = request.referencedTypeId,
                    referencedInlineAsmId = request.referencedInlineAsmId,
                ),
            )
        }
        statusMessage = "操作数已保存"
        refreshModuleScope()
    }

    suspend fun saveMetadataNode(selectedId: String?, request: CreateLlvmMetadataNodeRequest) {
        val moduleId = selectedModuleId ?: return
        val result = if (selectedId == null) {
            metadataService.createNode(request.copy(moduleId = moduleId))
        } else {
            metadataService.updateNode(selectedId, UpdateLlvmMetadataNodeRequest(request.name, request.kind, request.distinct))
        }
        selectedMetadataNodeId = result.id
        statusMessage = "Metadata 节点已保存"
        refreshModuleScope()
    }

    suspend fun removeSelectedMetadataNode() {
        selectedMetadataNodeId?.let {
            metadataService.deleteNode(it)
            statusMessage = "Metadata 节点已删除"
            refreshModuleScope()
        }
    }

    suspend fun saveCompileProfile(selectedId: String?, request: CreateLlvmCompileProfileRequest) {
        val moduleId = selectedModuleId ?: return
        val result = if (selectedId == null) {
            compileProfileService.create(request.copy(moduleId = moduleId))
        } else {
            compileProfileService.update(
                selectedId,
                UpdateLlvmCompileProfileRequest(
                    name = request.name,
                    targetPlatform = request.targetPlatform,
                    outputDirectory = request.outputDirectory,
                    optPath = request.optPath,
                    optArgs = request.optArgs,
                    llcPath = request.llcPath,
                    llcArgs = request.llcArgs,
                    clangPath = request.clangPath,
                    clangArgs = request.clangArgs,
                    environment = request.environment,
                ),
            )
        }
        selectedProfileId = result.id
        statusMessage = "编译配置已保存"
        refreshModuleScope()
    }

    suspend fun removeSelectedProfile() {
        selectedProfileId?.let {
            compileProfileService.delete(it)
            statusMessage = "编译配置已删除"
            refreshModuleScope()
        }
    }

    suspend fun exportSelectedModule(outputPath: String? = null) {
        val moduleId = selectedModuleId ?: return
        val exported = exportService.exportModule(moduleId, outputPath)
        exportPreviewText = exported.content
        statusMessage = "LLVM .ll 已导出预览"
    }

    suspend fun validateSelectedModule() {
        val moduleId = selectedModuleId ?: return
        validationIssues = validationService.validateModule(moduleId)
        statusMessage = if (validationIssues.isEmpty()) "校验通过" else "校验完成，共 ${validationIssues.size} 条问题"
    }

    suspend fun exportSnapshot() {
        val moduleId = selectedModuleId ?: return
        snapshotEditorText = json.encodeToString(snapshotService.exportModule(moduleId))
        statusMessage = "快照已导出"
    }

    suspend fun importSnapshot() {
        val snapshot = json.decodeFromString<LlvmSnapshotDto>(snapshotEditorText)
        snapshotService.importSnapshot(snapshot)
        statusMessage = "快照已导入"
        refreshAll()
    }

    suspend fun createAndRunCompileJob() {
        val moduleId = selectedModuleId ?: return
        val profileId = selectedProfileId ?: return
        val job = compileJobService.create(CreateLlvmCompileJobRequest(moduleId = moduleId, profileId = profileId, runNow = false))
        lastCompileResult = compileJobService.execute(job.id)
        statusMessage = "编译任务已执行"
        refreshModuleScope()
    }

    suspend fun refreshDeleteCheck(kind: String, id: String) {
        lastDeleteCheck = when (kind) {
            "module" -> moduleService.deleteCheck(id)
            "type" -> typeService.deleteCheck(id)
            "function" -> functionService.deleteCheck(id)
            "global" -> globalValueService.deleteGlobalCheck(id)
            "constant" -> globalValueService.deleteConstantCheck(id)
            "metadata-node" -> metadataService.deleteNodeCheck(id)
            "named-metadata" -> metadataService.deleteNamedCheck(id)
            "compile-profile" -> compileProfileService.deleteCheck(id)
            else -> null
        }
    }

    private fun clearDiagnostics() {
        validationIssues = emptyList()
        lastDeleteCheck = null
    }

    private fun loadLanguage(): PlaygroundUiLanguage {
        return runCatching { PlaygroundUiLanguage.valueOf(prefs.get("language", PlaygroundUiLanguage.ZH_CN.name)) }
            .getOrDefault(PlaygroundUiLanguage.ZH_CN)
    }
}
