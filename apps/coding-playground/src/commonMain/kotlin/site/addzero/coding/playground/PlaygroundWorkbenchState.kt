package site.addzero.coding.playground

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import org.koin.core.annotation.Single
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.*

@Single
class PlaygroundWorkbenchState(
    private val projectService: CodegenProjectService,
    private val targetService: GenerationTargetService,
    private val fileService: SourceFileService,
    private val declarationService: DeclarationService,
    private val renderService: CodeRenderService,
    private val artifactService: ManagedArtifactService,
    private val syncService: SyncService,
    private val kspIndexService: KspIndexService,
    private val managedFileSupport: ManagedFileSupport,
) {
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var autoSyncJob: Job? = null
    private var externalPollJob: Job? = null
    private var trackedOutputPath: String? = null
    private var trackedDiskHash: String? = null
    private var lastManagedWriteHash: String? = null
    private var dirtyFileId: String? = null

    var projects by mutableStateOf<List<CodegenProjectDto>>(emptyList())
        private set
    var targets by mutableStateOf<List<GenerationTargetDto>>(emptyList())
        private set
    var files by mutableStateOf<List<SourceFileMetaDto>>(emptyList())
        private set
    var declarations by mutableStateOf<List<DeclarationMetaDto>>(emptyList())
        private set
    var selectedProjectId by mutableStateOf<String?>(null)
        private set
    var selectedTargetId by mutableStateOf<String?>(null)
        private set
    var selectedFileId by mutableStateOf<String?>(null)
        private set
    var selectedDeclarationId by mutableStateOf<String?>(null)
        private set
    var projectAggregate by mutableStateOf<CodegenProjectAggregateDto?>(null)
        private set
    var fileAggregate by mutableStateOf<SourceFileAggregateDto?>(null)
        private set
    var targetPathPreview by mutableStateOf<PathPreviewDto?>(null)
        private set
    var sourcePreview by mutableStateOf<CodeRenderPreviewDto?>(null)
        private set
    var kspPreview by mutableStateOf<KspIndexPreviewDto?>(null)
        private set
    var artifacts by mutableStateOf<List<ManagedArtifactMetaDto>>(emptyList())
        private set
    var conflicts by mutableStateOf<List<SyncConflictMetaDto>>(emptyList())
        private set
    var validationIssues by mutableStateOf<List<ValidationIssueDto>>(emptyList())
        private set
    var searchQuery by mutableStateOf("")
    var statusMessage by mutableStateOf("Kotlin 声明式代码生成台已就绪")
        private set
    var statusIsError by mutableStateOf(false)
        private set
    var autoPreviewEnabled by mutableStateOf(true)
        private set
    var autoWriteEnabled by mutableStateOf(true)
        private set
    var autoImportExternalEnabled by mutableStateOf(true)
        private set
    var autoSyncInProgress by mutableStateOf(false)
        private set

    fun startBackgroundSync() {
        ensureExternalPolling()
    }

    fun stopBackgroundSync() {
        autoSyncJob?.cancel()
        externalPollJob?.cancel()
        backgroundScope.cancel()
    }

    fun updateAutoSyncSettings(
        autoPreviewEnabled: Boolean = this.autoPreviewEnabled,
        autoWriteEnabled: Boolean = this.autoWriteEnabled,
        autoImportExternalEnabled: Boolean = this.autoImportExternalEnabled,
    ) {
        this.autoPreviewEnabled = autoPreviewEnabled
        this.autoWriteEnabled = autoWriteEnabled
        this.autoImportExternalEnabled = autoImportExternalEnabled
        if (!autoPreviewEnabled && !autoWriteEnabled) {
            autoSyncJob?.cancel()
        } else if (dirtyFileId != null) {
            scheduleAutoSync()
        }
        ensureExternalPolling()
    }

    suspend fun refreshAll() {
        projects = projectService.list(CodegenSearchRequest(query = searchQuery.ifBlank { null }))
        if (selectedProjectId !in projects.map { it.id }) {
            selectedProjectId = projects.firstOrNull()?.id
        }
        refreshProjectScope()
    }

    suspend fun refreshProjectScope() {
        val projectId = selectedProjectId
        if (projectId == null) {
            projectAggregate = null
            targets = emptyList()
            files = emptyList()
            declarations = emptyList()
            selectedTargetId = null
            selectedFileId = null
            selectedDeclarationId = null
            clearDetails()
            return
        }
        val aggregate = projectService.aggregate(projectId)
        projectAggregate = aggregate
        targets = aggregate.targets
        if (selectedTargetId !in targets.map { it.id }) {
            selectedTargetId = targets.firstOrNull()?.id
        }
        files = aggregate.files.filter { it.targetId == selectedTargetId }
        if (selectedFileId !in files.map { it.id }) {
            selectedFileId = files.firstOrNull()?.id
        }
        declarations = aggregate.declarations.filter { it.fileId == selectedFileId }
        if (selectedDeclarationId !in declarations.map { it.id }) {
            selectedDeclarationId = declarations.firstOrNull()?.id
        }
        refreshFileScope()
    }

    suspend fun refreshFileScope() {
        val fileId = selectedFileId
        if (fileId == null) {
            fileAggregate = null
            targetPathPreview = selectedTargetId?.let { runCatching { targetService.previewPath(it) }.getOrNull() }
            sourcePreview = null
            artifacts = emptyList()
            conflicts = emptyList()
            kspPreview = null
            validationIssues = emptyList()
            clearTrackedFileState()
            return
        }
        fileAggregate = fileService.aggregate(fileId)
        declarations = fileAggregate?.declarations.orEmpty()
        if (selectedDeclarationId !in declarations.map { it.id }) {
            selectedDeclarationId = declarations.firstOrNull()?.id
        }
        targetPathPreview = selectedTargetId?.let { runCatching { targetService.previewPath(it) }.getOrNull() }
        sourcePreview = runCatching { renderService.previewFile(fileId) }.getOrNull()
        artifacts = artifactService.list(CodegenSearchRequest(fileId = fileId))
        conflicts = syncService.listConflicts(CodegenSearchRequest(fileId = fileId))
        validationIssues = fileService.validate(fileId) + (selectedDeclarationId?.let { declarationService.validate(it) }.orEmpty())
        selectedTargetId?.let { targetId ->
            kspPreview = runCatching { kspIndexService.previewIndex(targetId) }.getOrNull()
        }
        syncTrackedFileState()
    }

    fun selectProject(id: String?) {
        selectedProjectId = id
        selectedTargetId = null
        selectedFileId = null
        selectedDeclarationId = null
    }

    fun selectTarget(id: String?) {
        selectedTargetId = id
        selectedFileId = null
        selectedDeclarationId = null
        files = projectAggregate?.files?.filter { it.targetId == id }.orEmpty()
        selectedFileId = files.firstOrNull()?.id
        declarations = projectAggregate?.declarations?.filter { it.fileId == selectedFileId }.orEmpty()
        selectedDeclarationId = declarations.firstOrNull()?.id
    }

    fun selectFile(id: String?) {
        selectedFileId = id
        declarations = projectAggregate?.declarations?.filter { it.fileId == id }.orEmpty()
        selectedDeclarationId = declarations.firstOrNull()?.id
    }

    fun selectDeclaration(id: String?) {
        selectedDeclarationId = id
    }

    suspend fun saveProject(selectedId: String?, request: CreateCodegenProjectRequest) {
        val saved = if (selectedId == null) {
            projectService.create(request)
        } else {
            projectService.update(selectedId, UpdateCodegenProjectRequest(request.name, request.description))
        }
        selectedProjectId = saved.id
        updateStatus("项目已保存")
        refreshAll()
    }

    suspend fun saveTarget(selectedId: String?, request: CreateGenerationTargetRequest) {
        val saved = if (selectedId == null) {
            targetService.create(request)
        } else {
            targetService.update(
                selectedId,
                UpdateGenerationTargetRequest(
                    name = request.name,
                    rootDir = request.rootDir,
                    sourceSet = request.sourceSet,
                    basePackage = request.basePackage,
                    indexPackage = request.indexPackage,
                    kspEnabled = request.kspEnabled,
                    variables = request.variables,
                ),
            )
        }
        selectedTargetId = saved.id
        updateStatus("生成目标已保存")
        refreshProjectScope()
        markMetadataChanged()
    }

    suspend fun saveFile(selectedId: String?, request: CreateSourceFileRequest) {
        val saved = if (selectedId == null) {
            fileService.create(request)
        } else {
            fileService.update(
                selectedId,
                UpdateSourceFileRequest(
                    packageName = request.packageName,
                    fileName = request.fileName,
                    docComment = request.docComment,
                ),
            )
        }
        selectedFileId = saved.id
        updateStatus("Kotlin 文件已保存")
        refreshProjectScope()
        markMetadataChanged()
    }

    suspend fun saveDeclaration(selectedId: String?, request: CreateDeclarationRequest) {
        val saved = if (selectedId == null) {
            declarationService.create(request)
        } else {
            declarationService.update(
                selectedId,
                UpdateDeclarationRequest(
                    name = request.name,
                    visibility = request.visibility,
                    modifiers = request.modifiers,
                    superTypes = request.superTypes,
                    docComment = request.docComment,
                ),
            )
        }
        selectedDeclarationId = saved.id
        updateStatus("声明已保存")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun createPreset(kind: DeclarationKind, declarationName: String, packageName: String) {
        val targetId = selectedTargetId ?: return
        val aggregate = fileService.createPreset(
            CreateDeclarationPresetRequest(
                targetId = targetId,
                packageName = packageName,
                declarationName = declarationName,
                kind = kind,
            ),
        )
        selectedFileId = aggregate.file.id
        selectedDeclarationId = aggregate.declarations.firstOrNull()?.id
        updateStatus("已创建 ${declarationName} 预设")
        refreshProjectScope()
        markMetadataChanged()
    }

    suspend fun createScenePreset(
        preset: ScenePresetKind,
        featureName: String,
        packageName: String,
        routeSegment: String?,
        sceneTitle: String?,
    ) {
        val targetId = selectedTargetId ?: return
        val result = fileService.createScenePreset(
            CreateScenePresetRequest(
                targetId = targetId,
                packageName = packageName,
                featureName = featureName,
                preset = preset,
                routeSegment = routeSegment,
                sceneTitle = sceneTitle,
            ),
        )
        selectedFileId = result.primaryFileId
        updateStatus(result.notes.joinToString("；").ifBlank { result.message })
        refreshProjectScope()
        markMetadataChanged()
    }

    suspend fun addImport(importPath: String, alias: String?) {
        val fileId = selectedFileId ?: return
        fileService.createImport(CreateImportRequest(fileId, importPath, alias))
        updateStatus("导包已添加")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun deleteImport(id: String) {
        fileService.deleteImport(id)
        updateStatus("导包已删除")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun addConstructorParam(name: String, type: String, mutable: Boolean, defaultValue: String?) {
        val declarationId = selectedDeclarationId ?: return
        declarationService.createConstructorParam(
            CreateConstructorParamRequest(
                declarationId = declarationId,
                name = name,
                type = type,
                mutable = mutable,
                defaultValue = defaultValue,
            ),
        )
        updateStatus("构造参数已添加")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun deleteConstructorParam(id: String) {
        declarationService.deleteConstructorParam(id)
        updateStatus("构造参数已删除")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun addProperty(name: String, type: String, mutable: Boolean, initializer: String?) {
        val declarationId = selectedDeclarationId ?: return
        declarationService.createProperty(
            CreatePropertyRequest(
                declarationId = declarationId,
                name = name,
                type = type,
                mutable = mutable,
                initializer = initializer,
            ),
        )
        updateStatus("属性已添加")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun deleteProperty(id: String) {
        declarationService.deleteProperty(id)
        updateStatus("属性已删除")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun addEnumEntry(name: String) {
        val declarationId = selectedDeclarationId ?: return
        declarationService.createEnumEntry(CreateEnumEntryRequest(declarationId, name))
        updateStatus("枚举项已添加")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun deleteEnumEntry(id: String) {
        declarationService.deleteEnumEntry(id)
        updateStatus("枚举项已删除")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun addFunction(name: String, returnType: String, parametersText: String) {
        val declarationId = selectedDeclarationId ?: return
        declarationService.createFunctionStub(
            CreateFunctionStubRequest(
                declarationId = declarationId,
                name = name,
                returnType = returnType,
                parameters = parseFunctionParameters(parametersText),
                bodyMode = FunctionBodyMode.TEMPLATE,
            ),
        )
        updateStatus("函数桩已添加")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun deleteFunction(id: String) {
        declarationService.deleteFunctionStub(id)
        updateStatus("函数桩已删除")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun addDeclarationAnnotation(annotationClassName: String, argsText: String) {
        val declarationId = selectedDeclarationId ?: return
        val usage = declarationService.createAnnotationUsage(
            CreateAnnotationUsageRequest(
                ownerType = AnnotationOwnerType.DECLARATION,
                ownerId = declarationId,
                annotationClassName = annotationClassName,
            ),
        )
        parseAnnotationArguments(argsText).forEach { argument ->
            declarationService.createAnnotationArgument(
                CreateAnnotationArgumentRequest(
                    annotationUsageId = usage.id,
                    name = argument.first,
                    value = argument.second,
                ),
            )
        }
        updateStatus("声明注解已添加")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun deleteAnnotationUsage(id: String) {
        declarationService.deleteAnnotationUsage(id)
        updateStatus("声明注解已删除")
        refreshFileScope()
        markMetadataChanged()
    }

    suspend fun exportSelectedFile() {
        autoSyncJob?.cancel()
        val fileId = selectedFileId ?: return
        exportFileInternal(fileId = fileId, autoTriggered = false)
    }

    suspend fun importSelectedFile() {
        autoSyncJob?.cancel()
        importSelectedFileInternal(autoTriggered = false)
    }

    suspend fun refreshPreview() {
        val fileId = selectedFileId ?: return
        sourcePreview = renderService.previewFile(fileId)
        selectedTargetId?.let { targetId ->
            kspPreview = kspIndexService.previewIndex(targetId)
        }
        artifacts = artifactService.list(CodegenSearchRequest(fileId = fileId))
        conflicts = syncService.listConflicts(CodegenSearchRequest(fileId = fileId))
    }

    suspend fun resolveConflict(conflictId: String, resolution: SyncConflictResolution) {
        syncService.resolveConflict(conflictId, ResolveSyncConflictRequest(resolution))
        updateStatus("冲突已处理")
        refreshFileScope()
        dirtyFileId = null
    }

    suspend fun deleteSelectedProject() {
        selectedProjectId?.let {
            projectService.delete(it)
            updateStatus("项目已删除")
            refreshAll()
        }
    }

    suspend fun deleteSelectedTarget() {
        selectedTargetId?.let {
            targetService.delete(it)
            updateStatus("生成目标已删除")
            refreshProjectScope()
        }
    }

    suspend fun deleteSelectedFile() {
        selectedFileId?.let {
            fileService.delete(it)
            updateStatus("文件已删除")
            refreshProjectScope()
        }
    }

    suspend fun deleteSelectedDeclaration() {
        selectedDeclarationId?.let {
            declarationService.delete(it)
            updateStatus("声明已删除")
            refreshFileScope()
            markMetadataChanged()
        }
    }

    fun reportError(throwable: Throwable) {
        val message = throwable.message?.takeIf { it.isNotBlank() } ?: throwable::class.simpleName ?: "未知错误"
        updateStatus("操作失败：$message", isError = true)
    }

    fun createDefaultProjectName(): String {
        val usedNames = projects.map { it.name.lowercase() }.toSet()
        val baseName = "未命名项目"
        if (baseName.lowercase() !in usedNames) {
            return baseName
        }
        var index = 2
        while (true) {
            val candidate = "$baseName $index"
            if (candidate.lowercase() !in usedNames) {
                return candidate
            }
            index += 1
        }
    }

    suspend fun awaitAutoSyncSettled(timeoutMillis: Long = 6_000L) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            val autoSyncActive = autoSyncJob?.isActive == true
            if (!autoSyncInProgress && !autoSyncActive) {
                return
            }
            delay(50)
        }
    }

    private fun clearDetails() {
        fileAggregate = null
        targetPathPreview = null
        sourcePreview = null
        kspPreview = null
        artifacts = emptyList()
        conflicts = emptyList()
        validationIssues = emptyList()
        clearTrackedFileState()
    }

    private fun parseFunctionParameters(text: String): List<FunctionParameterDto> {
        return text.lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .map { line ->
                val name = line.substringBefore(":").trim()
                val typeAndDefault = line.substringAfter(":", "").trim()
                val type = typeAndDefault.substringBefore("=").trim()
                val defaultValue = typeAndDefault.substringAfter("=", "").takeIf { typeAndDefault.contains("=") }?.trim()
                FunctionParameterDto(
                    name = name,
                    type = type,
                    nullable = type.endsWith("?"),
                    defaultValue = defaultValue,
                )
            }
            .toList()
    }

    private fun parseAnnotationArguments(text: String): List<Pair<String?, String>> {
        return text.split(',')
            .map(String::trim)
            .filter(String::isNotBlank)
            .map { token ->
                if (token.contains("=")) {
                    token.substringBefore("=").trim() to token.substringAfter("=").trim()
                } else {
                    null to token
                }
            }
    }

    private fun markMetadataChanged() {
        dirtyFileId = selectedFileId
        if (dirtyFileId != null) {
            scheduleAutoSync()
        }
    }

    private fun scheduleAutoSync() {
        if (!autoPreviewEnabled && !autoWriteEnabled) {
            return
        }
        autoSyncJob?.cancel()
        autoSyncJob = backgroundScope.launch {
            delay(1_200)
            runCatching { runAutoSyncNow() }
                .onFailure(::reportError)
        }
    }

    private fun ensureExternalPolling() {
        if (externalPollJob?.isActive == true) {
            return
        }
        externalPollJob = backgroundScope.launch {
            while (isActive) {
                delay(2_000)
                runCatching { pollExternalFileChanges() }
                    .onFailure(::reportError)
            }
        }
    }

    private suspend fun runAutoSyncNow() {
        val fileId = dirtyFileId ?: return
        if (selectedFileId == null && !autoWriteEnabled) {
            return
        }
        autoSyncInProgress = true
        try {
            if (autoPreviewEnabled && selectedFileId == fileId) {
                refreshPreview()
            }
            if (autoWriteEnabled && dirtyFileId == fileId) {
                exportFileInternal(fileId = fileId, autoTriggered = true)
            }
        } finally {
            autoSyncInProgress = false
        }
    }

    private suspend fun pollExternalFileChanges() {
        if (!autoImportExternalEnabled || autoSyncInProgress || dirtyFileId != null) {
            return
        }
        val outputPath = trackedOutputPath ?: sourcePreview?.outputPath ?: return
        val currentDiskHash = readFileHashOrNull(outputPath) ?: return
        if (trackedOutputPath != outputPath) {
            trackedOutputPath = outputPath
            trackedDiskHash = currentDiskHash
            return
        }
        if (currentDiskHash == trackedDiskHash || currentDiskHash == lastManagedWriteHash) {
            trackedDiskHash = currentDiskHash
            return
        }
        trackedDiskHash = currentDiskHash
        importSelectedFileInternal(autoTriggered = true)
    }

    private suspend fun exportFileInternal(fileId: String, autoTriggered: Boolean) {
        val result = syncService.export(SyncExportRequest(fileId = fileId))
        val hasConflicts = result.conflicts.isNotEmpty()
        if (selectedFileId == fileId) {
            refreshFileScope()
        }
        if (!hasConflicts && dirtyFileId == fileId) {
            dirtyFileId = null
        }
        val preview = if (selectedFileId == fileId) {
            sourcePreview
        } else {
            result.previews.firstOrNull()
        }
        if (selectedFileId == fileId) {
            preview?.let {
                trackedOutputPath = it.outputPath
                val contentHash = managedFileSupport.hashContent(it.content)
                trackedDiskHash = contentHash
                lastManagedWriteHash = contentHash
            }
        }
        val fileName = preview?.file?.fileName ?: files.firstOrNull { it.id == fileId }?.fileName.orEmpty()
        updateStatus(
            message = when {
                autoTriggered && !hasConflicts -> "已自动写盘 $fileName".trim()
                autoTriggered -> result.messages.joinToString("；").ifBlank { "自动写盘时出现冲突" }
                else -> result.messages.joinToString("；").ifBlank { "源码已写盘" }
            },
            isError = hasConflicts,
        )
    }

    private suspend fun importSelectedFileInternal(autoTriggered: Boolean) {
        val fileId = selectedFileId ?: return
        val result = syncService.importSource(SyncImportRequest(fileId = fileId))
        refreshProjectScope()
        val hasConflicts = result.conflicts.isNotEmpty()
        if (!hasConflicts) {
            dirtyFileId = null
        }
        trackedOutputPath = sourcePreview?.outputPath
        trackedDiskHash = trackedOutputPath?.let(::readFileHashOrNull)
        updateStatus(
            message = when {
                autoTriggered && !hasConflicts -> "已自动导回外部改动"
                autoTriggered -> result.messages.joinToString("；").ifBlank { "自动导回时出现冲突" }
                else -> result.messages.joinToString("；").ifBlank { "源码已导回" }
            },
            isError = hasConflicts,
        )
    }

    private fun syncTrackedFileState() {
        val outputPath = sourcePreview?.outputPath
        if (outputPath == null) {
            clearTrackedFileState()
            return
        }
        if (trackedOutputPath != outputPath) {
            trackedOutputPath = outputPath
            trackedDiskHash = readFileHashOrNull(outputPath)
        }
    }

    private fun clearTrackedFileState() {
        trackedOutputPath = null
        trackedDiskHash = null
        lastManagedWriteHash = null
        dirtyFileId = null
    }

    private fun readFileHashOrNull(pathText: String): String? {
        return managedFileSupport.readFileHashOrNull(pathText)
    }

    private fun updateStatus(message: String, isError: Boolean = false) {
        statusMessage = message
        statusIsError = isError
    }
}
