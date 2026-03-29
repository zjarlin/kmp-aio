package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.ImportMeta
import site.addzero.coding.playground.server.entity.SourceFileMeta
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.server.util.ensureKtFileName
import site.addzero.coding.playground.server.util.toKebabCase
import site.addzero.coding.playground.server.util.toPascalIdentifier
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.DeclarationService
import site.addzero.coding.playground.shared.service.SourceFileService

@Single
class SourceFileServiceImpl(
    private val support: MetadataPersistenceSupport,
    private val serviceSupport: CodegenServiceSupport,
    private val declarationService: DeclarationService,
) : SourceFileService {
    override suspend fun create(request: CreateSourceFileRequest): SourceFileMetaDto {
        val target = support.targetOrThrow(request.targetId)
        val normalizedFileName = serviceSupport.requireFileName(request.fileName)
        serviceSupport.requirePackageName(request.packageName, "文件包名")
        if (support.listFiles(target.id).any { it.packageName == request.packageName && it.fileName == normalizedFileName }) {
            throw PlaygroundValidationException("同一目标下包名和文件名组合不能重复")
        }
        val now = support.now()
        val entity = new(SourceFileMeta::class).by {
            id = support.newId()
            project = support.projectRef(target.projectId)
            this.target = support.targetRef(target.id)
            packageName = request.packageName.trim()
            fileName = normalizedFileName
            docComment = request.docComment?.trim()?.ifBlank { null }
            orderIndex = serviceSupport.nextOrder(support.listFiles(target.id).map { it.orderIndex })
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun createPreset(request: CreateDeclarationPresetRequest): SourceFileAggregateDto {
        val target = support.targetOrThrow(request.targetId)
        val declarationName = request.declarationName.trim()
        serviceSupport.requireIdentifier(declarationName, "声明名称")
        val file = create(
            CreateSourceFileRequest(
                targetId = target.id,
                packageName = request.packageName.ifBlank { target.basePackage },
                fileName = declarationName.ensureKtFileName(),
            ),
        )
        val declaration = declarationService.create(
            CreateDeclarationRequest(
                fileId = file.id,
                name = declarationName,
                kind = request.kind,
            ),
        )
        when (request.kind) {
            DeclarationKind.DATA_CLASS -> {
                declarationService.createConstructorParam(CreateConstructorParamRequest(declaration.id, "id", "String"))
                declarationService.createConstructorParam(CreateConstructorParamRequest(declaration.id, "name", "String", defaultValue = "\"\""))
            }

            DeclarationKind.CLASS -> {
                declarationService.createProperty(
                    CreatePropertyRequest(
                        declarationId = declaration.id,
                        name = "statusMessage",
                        type = "String",
                        mutable = true,
                        initializer = "\"待补充\"",
                    ),
                )
                declarationService.createFunctionStub(
                    CreateFunctionStubRequest(
                        declarationId = declaration.id,
                        name = "refresh",
                        returnType = "Unit",
                        bodyMode = FunctionBodyMode.TEMPLATE,
                    ),
                )
            }

            DeclarationKind.ENUM_CLASS -> {
                declarationService.createEnumEntry(CreateEnumEntryRequest(declaration.id, "DEFAULT"))
                declarationService.createEnumEntry(CreateEnumEntryRequest(declaration.id, "DISABLED"))
            }

            DeclarationKind.INTERFACE -> {
                declarationService.createFunctionStub(
                    CreateFunctionStubRequest(
                        declarationId = declaration.id,
                        name = "load",
                        returnType = declaration.name,
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyMode = FunctionBodyMode.TEMPLATE,
                    ),
                )
            }

            DeclarationKind.OBJECT -> {
                declarationService.createProperty(
                    CreatePropertyRequest(
                        declarationId = declaration.id,
                        name = "version",
                        type = "String",
                        initializer = "\"1.0.0\"",
                    ),
                )
            }

            DeclarationKind.ANNOTATION_CLASS -> {
                declarationService.createConstructorParam(CreateConstructorParamRequest(declaration.id, "value", "String", defaultValue = "\"\""))
            }
        }
        return aggregate(file.id)
    }

    override suspend fun createScenePreset(request: CreateScenePresetRequest): ScenePresetResultDto {
        val primaryTarget = support.targetOrThrow(request.targetId).toDto()
        val packageName = request.packageName.ifBlank { primaryTarget.basePackage }.trim()
        serviceSupport.requirePackageName(packageName, "场景包名")
        serviceSupport.requireText(request.featureName, "场景名称")
        val featureName = request.featureName.toPascalIdentifier()
        val routeSegment = request.routeSegment?.trim()?.takeIf { it.isNotBlank() } ?: request.featureName.toKebabCase()
        val sceneTitle = request.sceneTitle?.trim()?.takeIf { it.isNotBlank() } ?: featureName
        val targetBundle = resolveSceneTargets(primaryTarget.id, request.includeSiblingTargets)
        val notes = targetBundle.notes.toMutableList()
        val fileIds = when (request.preset) {
            ScenePresetKind.BUSINESS_CRUD -> createBusinessCrudScene(
                packageName = packageName,
                featureName = featureName,
                routeSegment = routeSegment,
                sceneTitle = sceneTitle,
                targetBundle = targetBundle,
                notes = notes,
            )

            ScenePresetKind.SKILL_DOTFILE -> createSkillDotfileScene(
                packageName = packageName,
                featureName = featureName,
                routeSegment = routeSegment,
                sceneTitle = sceneTitle,
                targetBundle = targetBundle,
                notes = notes,
            )

            ScenePresetKind.KCLOUD_PLUGIN -> createKcloudPluginScene(
                packageName = packageName,
                featureName = featureName,
                routeSegment = routeSegment,
                sceneTitle = sceneTitle,
                targetBundle = targetBundle,
                notes = notes,
            )
        }
        val createdFiles = fileIds.distinct().map { support.fileOrThrow(it).toDto() }
        return ScenePresetResultDto(
            preset = request.preset,
            primaryFileId = createdFiles.firstOrNull()?.id,
            createdFiles = createdFiles,
            affectedTargetIds = createdFiles.map { it.targetId }.distinct(),
            notes = notes,
            message = "${request.preset.displayName()} 已生成 ${createdFiles.size} 个文件",
        )
    }

    override suspend fun list(search: CodegenSearchRequest): List<SourceFileMetaDto> {
        return support.listFiles(search.targetId)
            .filter {
                search.matches(
                    projectId = it.projectId,
                    targetId = it.targetId,
                    fileId = it.id,
                    values = listOf(it.packageName, it.fileName, it.docComment),
                )
            }
            .map { it.toDto() }
    }

    override suspend fun get(id: String): SourceFileMetaDto = support.fileOrThrow(id).toDto()

    override suspend fun aggregate(id: String): SourceFileAggregateDto = support.buildFileAggregate(id)

    override suspend fun update(id: String, request: UpdateSourceFileRequest): SourceFileMetaDto {
        val existing = support.fileOrThrow(id)
        val normalizedFileName = serviceSupport.requireFileName(request.fileName)
        serviceSupport.requirePackageName(request.packageName, "文件包名")
        if (support.listFiles(existing.targetId).any { it.id != id && it.packageName == request.packageName && it.fileName == normalizedFileName }) {
            throw PlaygroundValidationException("同一目标下包名和文件名组合不能重复")
        }
        val updated = support.inTransaction {
            val entity = new(SourceFileMeta::class).by {
                this.id = id
                project = support.projectRef(existing.projectId)
                target = support.targetRef(existing.targetId)
                packageName = request.packageName.trim()
                fileName = normalizedFileName
                docComment = request.docComment?.trim()?.ifBlank { null }
                orderIndex = existing.orderIndex
                createdAt = existing.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity).modifiedEntity
        }
        support.listDeclarations(id).forEach { declaration ->
            val entity = new(site.addzero.coding.playground.server.entity.DeclarationMeta::class).by {
                this.id = declaration.id
                this.file = support.fileRef(id)
                targetId = existing.targetId
                packageName = request.packageName.trim()
                fqName = serviceSupport.buildFqName(request.packageName.trim(), declaration.name)
                name = declaration.name
                kind = declaration.kind
                visibility = declaration.visibility
                modifiersJson = declaration.modifiersJson
                superTypesJson = declaration.superTypesJson
                docComment = declaration.docComment
                orderIndex = declaration.orderIndex
                createdAt = declaration.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity)
        }
        return updated.toDto()
    }

    override suspend fun deleteCheck(id: String): DeleteCheckResultDto {
        val aggregate = support.buildFileAggregate(id)
        return DeleteCheckResultDto(
            id = id,
            kind = "file",
            canDelete = true,
            warnings = listOf("删除文件 ${aggregate.file.fileName} 将清理 ${aggregate.declarations.size} 个声明和相关同步记录"),
        )
    }

    override suspend fun validate(id: String): List<ValidationIssueDto> {
        val aggregate = support.buildFileAggregate(id)
        val issues = mutableListOf<ValidationIssueDto>()
        if (aggregate.declarations.isEmpty()) {
            issues += serviceSupport.buildValidationIssue("file", id, ValidationSeverity.WARNING, "文件里还没有声明")
        }
        if (aggregate.imports.map { it.importPath }.distinct().size != aggregate.imports.size) {
            issues += serviceSupport.buildValidationIssue("file", id, ValidationSeverity.ERROR, "文件存在重复导包")
        }
        return issues
    }

    override suspend fun delete(id: String) {
        support.fileOrThrow(id)
        support.inTransaction {
            support.deleteFileCascade(id)
        }
    }

    override suspend fun createImport(request: CreateImportRequest): ImportMetaDto {
        support.fileOrThrow(request.fileId)
        serviceSupport.requireText(request.importPath, "导包路径")
        val existing = support.listImports(request.fileId)
        val now = support.now()
        val entity = new(ImportMeta::class).by {
            id = support.newId()
            file = support.fileRef(request.fileId)
            importPath = request.importPath.trim()
            alias = request.alias?.trim()?.ifBlank { null }
            orderIndex = serviceSupport.nextOrder(existing.map { it.orderIndex })
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateImport(id: String, request: UpdateImportRequest): ImportMetaDto {
        val existing = support.importOrThrow(id)
        serviceSupport.requireText(request.importPath, "导包路径")
        val entity = new(ImportMeta::class).by {
            this.id = id
            file = support.fileRef(existing.fileId)
            importPath = request.importPath.trim()
            alias = request.alias?.trim()?.ifBlank { null }
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteImport(id: String) {
        support.importOrThrow(id)
        support.sqlClient.deleteById(ImportMeta::class, id)
    }

    override suspend fun reorderImports(fileId: String, request: ReorderRequestDto): List<ImportMetaDto> {
        val items = support.listImports(fileId)
        if (items.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("导包排序列表不完整")
        }
        return request.orderedIds.mapIndexed { index, id ->
            val existing = items.first { it.id == id }
            val entity = new(ImportMeta::class).by {
                this.id = id
                file = support.fileRef(fileId)
                importPath = existing.importPath
                alias = existing.alias
                orderIndex = index
                createdAt = existing.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity).modifiedEntity.toDto()
        }
    }

    private fun resolveSceneTargets(primaryTargetId: String, includeSiblingTargets: Boolean): SceneTargetBundle {
        val primary = support.targetOrThrow(primaryTargetId).toDto()
        if (!includeSiblingTargets) {
            return if (primary.sourceSet == "jvmMain") {
                SceneTargetBundle(primary = primary, commonTarget = null, jvmTarget = primary)
            } else {
                SceneTargetBundle(primary = primary, commonTarget = primary, jvmTarget = null)
            }
        }
        val siblings = support.listTargets(primary.projectId)
            .asSequence()
            .map { it.toDto() }
            .filter { it.id != primary.id && it.rootDir == primary.rootDir }
            .toList()
        val commonTarget = when (primary.sourceSet) {
            "jvmMain" -> siblings.firstOrNull { it.sourceSet == "commonMain" }
            else -> primary
        }
        val jvmTarget = when (primary.sourceSet) {
            "jvmMain" -> primary
            else -> siblings.firstOrNull { it.sourceSet == "jvmMain" }
        }
        val notes = buildList {
            if (commonTarget == null) {
                add("未找到 commonMain 目标，公共 DTO / ClientApi / ViewState / Compose 页壳已跳过。")
            } else if (commonTarget.id != primary.id) {
                add("已自动联动 commonMain 目标：${commonTarget.name}")
            }
            if (jvmTarget == null) {
                add("未找到 jvmMain 目标，Repository / Service / Route 服务端壳已跳过。")
            } else if (jvmTarget.id != primary.id) {
                add("已自动联动 jvmMain 目标：${jvmTarget.name}")
            }
        }
        return SceneTargetBundle(
            primary = primary,
            commonTarget = commonTarget,
            jvmTarget = jvmTarget,
            notes = notes,
        )
    }

    private suspend fun createBusinessCrudScene(
        packageName: String,
        featureName: String,
        routeSegment: String,
        sceneTitle: String,
        targetBundle: SceneTargetBundle,
        notes: MutableList<String>,
    ): List<String> {
        val fileIds = mutableListOf<String>()
        val dtoPackage = "$packageName.dto"
        val clientPackage = "$packageName.client"
        val statePackage = "$packageName.state"
        val screenPackage = "$packageName.screen"
        val diPackage = "$packageName.di"
        val repositoryPackage = "$packageName.repository"
        val servicePackage = "$packageName.service"
        val routePackage = "$packageName.route"

        targetBundle.commonTarget?.let { commonTarget ->
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = dtoPackage,
                declarationName = "${featureName}Request",
                kind = DeclarationKind.DATA_CLASS,
                docComment = "$sceneTitle 新增与编辑请求。",
                constructorParams = listOf(
                    ctor(name = "name", type = "String", defaultValue = "\"\""),
                    ctor(name = "code", type = "String", defaultValue = "\"\""),
                    ctor(name = "remark", type = "String", defaultValue = "\"\""),
                ),
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = dtoPackage,
                declarationName = "${featureName}Response",
                kind = DeclarationKind.DATA_CLASS,
                docComment = "$sceneTitle 查询响应。",
                constructorParams = listOf(
                    ctor(name = "id", type = "String", defaultValue = "\"\""),
                    ctor(name = "name", type = "String", defaultValue = "\"\""),
                    ctor(name = "code", type = "String", defaultValue = "\"\""),
                    ctor(name = "status", type = "String", defaultValue = "\"DRAFT\""),
                    ctor(name = "remark", type = "String", defaultValue = "\"\""),
                ),
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = dtoPackage,
                declarationName = "${featureName}Query",
                kind = DeclarationKind.DATA_CLASS,
                docComment = "$sceneTitle 列表查询条件。",
                constructorParams = listOf(
                    ctor(name = "keyword", type = "String", defaultValue = "\"\""),
                    ctor(name = "status", type = "String?", nullable = true, defaultValue = "null"),
                ),
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = dtoPackage,
                declarationName = "${featureName}PageRequest",
                kind = DeclarationKind.DATA_CLASS,
                docComment = "$sceneTitle 分页请求。",
                constructorParams = listOf(
                    ctor(name = "page", type = "Int", defaultValue = "1"),
                    ctor(name = "pageSize", type = "Int", defaultValue = "20"),
                ),
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = clientPackage,
                declarationName = "${featureName}ClientApi",
                kind = DeclarationKind.CLASS,
                docComment = "$sceneTitle Client API 壳。",
                imports = listOf(
                    import("io.insert-koin.core.annotation.Single"),
                    import("$dtoPackage.${featureName}PageRequest"),
                    import("$dtoPackage.${featureName}Query"),
                    import("$dtoPackage.${featureName}Request"),
                    import("$dtoPackage.${featureName}Response"),
                ),
                declarationAnnotations = listOf(annotation("Single")),
                functions = listOf(
                    fn(
                        name = "page",
                        returnType = "List<${featureName}Response>",
                        parameters = listOf(
                            FunctionParameterDto("pageRequest", "${featureName}PageRequest"),
                            FunctionParameterDto("query", "${featureName}Query"),
                        ),
                        bodyText = "TODO(\"调用 ${featureName} 分页接口\")",
                    ),
                    fn(
                        name = "get",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"调用 ${featureName} 详情接口\")",
                    ),
                    fn(
                        name = "create",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("request", "${featureName}Request")),
                        bodyText = "TODO(\"调用 ${featureName} 新增接口\")",
                    ),
                    fn(
                        name = "update",
                        returnType = "${featureName}Response",
                        parameters = listOf(
                            FunctionParameterDto("id", "String"),
                            FunctionParameterDto("request", "${featureName}Request"),
                        ),
                        bodyText = "TODO(\"调用 ${featureName} 更新接口\")",
                    ),
                    fn(
                        name = "deleteById",
                        returnType = "Unit",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"调用 ${featureName} 删除接口\")",
                    ),
                ),
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = statePackage,
                declarationName = "${featureName}ViewState",
                kind = DeclarationKind.CLASS,
                docComment = "$sceneTitle 页面状态壳。",
                imports = listOf(
                    import("io.insert-koin.core.annotation.Single"),
                    import("$dtoPackage.${featureName}Request"),
                    import("$dtoPackage.${featureName}Response"),
                ),
                declarationAnnotations = listOf(annotation("Single")),
                properties = listOf(
                    prop(name = "keyword", type = "String", mutable = true, initializer = "\"\""),
                    prop(name = "items", type = "List<${featureName}Response>", mutable = true, initializer = "emptyList()"),
                    prop(name = "selectedDetail", type = "${featureName}Response?", mutable = true, nullable = true, initializer = "null"),
                    prop(name = "editingRequest", type = "${featureName}Request", mutable = true, initializer = "${featureName}Request()"),
                    prop(name = "loading", type = "Boolean", mutable = true, initializer = "false"),
                    prop(name = "feedbackMessage", type = "String", mutable = true, initializer = "\"待接入真实调用\""),
                ),
                functions = listOf(
                    fn(name = "refresh", returnType = "Unit", bodyText = "TODO(\"刷新 ${sceneTitle} 列表\")"),
                    fn(
                        name = "loadDetail",
                        returnType = "Unit",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"加载 ${sceneTitle} 详情\")",
                    ),
                    fn(name = "startCreate", returnType = "Unit", bodyText = "editingRequest = ${featureName}Request()"),
                    fn(
                        name = "submit",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("id", "String?", nullable = true, defaultValue = "null")),
                        bodyText = "TODO(\"提交 ${sceneTitle} 编辑\")",
                    ),
                    fn(
                        name = "remove",
                        returnType = "Unit",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"删除 ${sceneTitle}\")",
                    ),
                ),
            )
            fileIds += createComposePageShell(
                target = commonTarget,
                packageName = screenPackage,
                declarationName = "${featureName}ListPage",
                statePackage = statePackage,
                stateType = "${featureName}ViewState",
                bodyText = "Text(\"${sceneTitle} 列表页壳，后续接入 AddTable、筛选区和分页。\")",
            )
            fileIds += createComposePageShell(
                target = commonTarget,
                packageName = screenPackage,
                declarationName = "${featureName}DetailPage",
                statePackage = statePackage,
                stateType = "${featureName}ViewState",
                bodyText = "Text(\"${sceneTitle} 详情页壳，后续接入基础信息和时间线。\")",
            )
            fileIds += createComposePageShell(
                target = commonTarget,
                packageName = screenPackage,
                declarationName = "${featureName}EditPage",
                statePackage = statePackage,
                stateType = "${featureName}ViewState",
                bodyText = "Text(\"${sceneTitle} 编辑页壳，后续接入表单和提交动作。\")",
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = diPackage,
                declarationName = "${featureName}CommonKoinModule",
                kind = DeclarationKind.CLASS,
                docComment = "把 $sceneTitle 的 commonMain 侧 ClientApi、ViewState、Compose 页面壳纳入 Koin 编译期聚合。",
                imports = listOf(
                    import("io.insert-koin.core.annotation.ComponentScan"),
                    import("io.insert-koin.core.annotation.Module"),
                ),
                declarationAnnotations = listOf(
                    annotation("Module"),
                    annotation("ComponentScan", arg("\"$packageName\"")),
                ),
            )
        }

        targetBundle.jvmTarget?.let { jvmTarget ->
            fileIds += createStructuredFile(
                target = jvmTarget,
                packageName = repositoryPackage,
                declarationName = "${featureName}Repository",
                kind = DeclarationKind.INTERFACE,
                docComment = "$sceneTitle Repository 壳。",
                imports = listOf(
                    import("$dtoPackage.${featureName}PageRequest"),
                    import("$dtoPackage.${featureName}Query"),
                    import("$dtoPackage.${featureName}Request"),
                    import("$dtoPackage.${featureName}Response"),
                ),
                functions = listOf(
                    interfaceFn(
                        name = "page",
                        returnType = "List<${featureName}Response>",
                        parameters = listOf(
                            FunctionParameterDto("pageRequest", "${featureName}PageRequest"),
                            FunctionParameterDto("query", "${featureName}Query"),
                        ),
                    ),
                    interfaceFn(
                        name = "get",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                    ),
                    interfaceFn(
                        name = "create",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("request", "${featureName}Request")),
                    ),
                    interfaceFn(
                        name = "update",
                        returnType = "${featureName}Response",
                        parameters = listOf(
                            FunctionParameterDto("id", "String"),
                            FunctionParameterDto("request", "${featureName}Request"),
                        ),
                    ),
                    interfaceFn(
                        name = "deleteById",
                        returnType = "Unit",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                    ),
                ),
            )
            fileIds += createStructuredFile(
                target = jvmTarget,
                packageName = servicePackage,
                declarationName = "${featureName}Service",
                kind = DeclarationKind.CLASS,
                docComment = "$sceneTitle Service 壳。",
                imports = listOf(
                    import("io.insert-koin.core.annotation.Single"),
                    import("$dtoPackage.${featureName}PageRequest"),
                    import("$dtoPackage.${featureName}Query"),
                    import("$dtoPackage.${featureName}Request"),
                    import("$dtoPackage.${featureName}Response"),
                ),
                declarationAnnotations = listOf(annotation("Single")),
                functions = listOf(
                    fn(
                        name = "page",
                        returnType = "List<${featureName}Response>",
                        parameters = listOf(
                            FunctionParameterDto("pageRequest", "${featureName}PageRequest"),
                            FunctionParameterDto("query", "${featureName}Query"),
                        ),
                        bodyText = "TODO(\"补齐 ${sceneTitle} 分页业务规则\")",
                    ),
                    fn(
                        name = "get",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"补齐 ${sceneTitle} 详情查询\")",
                    ),
                    fn(
                        name = "create",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("request", "${featureName}Request")),
                        bodyText = "TODO(\"补齐 ${sceneTitle} 新增逻辑\")",
                    ),
                    fn(
                        name = "update",
                        returnType = "${featureName}Response",
                        parameters = listOf(
                            FunctionParameterDto("id", "String"),
                            FunctionParameterDto("request", "${featureName}Request"),
                        ),
                        bodyText = "TODO(\"补齐 ${sceneTitle} 更新逻辑\")",
                    ),
                    fn(
                        name = "deleteById",
                        returnType = "Unit",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"补齐 ${sceneTitle} 删除逻辑\")",
                    ),
                ),
            )
            fileIds += createStructuredFile(
                target = jvmTarget,
                packageName = routePackage,
                declarationName = "${featureName}RouteShell",
                kind = DeclarationKind.CLASS,
                docComment = buildString {
                    appendLine("$sceneTitle spring2ktor 路由壳。")
                    appendLine("POST /api/$routeSegment/page")
                    appendLine("GET /api/$routeSegment/{id}")
                    appendLine("POST /api/$routeSegment")
                    appendLine("PUT /api/$routeSegment/{id}")
                    append("DELETE /api/$routeSegment/{id}")
                },
                imports = listOf(
                    import("$dtoPackage.${featureName}PageRequest"),
                    import("$dtoPackage.${featureName}Query"),
                    import("$dtoPackage.${featureName}Request"),
                    import("$dtoPackage.${featureName}Response"),
                ),
                functions = listOf(
                    fn(
                        name = "page",
                        returnType = "List<${featureName}Response>",
                        parameters = listOf(
                            FunctionParameterDto("pageRequest", "${featureName}PageRequest"),
                            FunctionParameterDto("query", "${featureName}Query"),
                        ),
                        bodyText = "TODO(\"在顶层 spring2ktor route 中转调 ${featureName}Service.page\")",
                    ),
                    fn(
                        name = "get",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"在顶层 spring2ktor route 中转调 ${featureName}Service.get\")",
                    ),
                    fn(
                        name = "create",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("request", "${featureName}Request")),
                        bodyText = "TODO(\"在顶层 spring2ktor route 中转调 ${featureName}Service.create\")",
                    ),
                    fn(
                        name = "update",
                        returnType = "${featureName}Response",
                        parameters = listOf(
                            FunctionParameterDto("id", "String"),
                            FunctionParameterDto("request", "${featureName}Request"),
                        ),
                        bodyText = "TODO(\"在顶层 spring2ktor route 中转调 ${featureName}Service.update\")",
                    ),
                    fn(
                        name = "deleteById",
                        returnType = "Unit",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"在顶层 spring2ktor route 中转调 ${featureName}Service.deleteById\")",
                    ),
                ),
            )
            fileIds += createStructuredFile(
                target = jvmTarget,
                packageName = diPackage,
                declarationName = "${featureName}ServerKoinModule",
                kind = DeclarationKind.CLASS,
                docComment = "把 $sceneTitle 的 jvmMain 侧 Repository、Service 和路由壳纳入 Koin 编译期聚合。",
                imports = listOf(
                    import("io.insert-koin.core.annotation.ComponentScan"),
                    import("io.insert-koin.core.annotation.Module"),
                ),
                declarationAnnotations = listOf(
                    annotation("Module"),
                    annotation("ComponentScan", arg("\"$packageName\"")),
                ),
            )
        }
        return fileIds
    }

    private suspend fun createSkillDotfileScene(
        packageName: String,
        featureName: String,
        routeSegment: String,
        sceneTitle: String,
        targetBundle: SceneTargetBundle,
        notes: MutableList<String>,
    ): List<String> {
        val fileIds = mutableListOf<String>()
        val dtoPackage = "$packageName.dto"
        val clientPackage = "$packageName.client"
        val statePackage = "$packageName.state"
        val screenPackage = "$packageName.screen"
        val diPackage = "$packageName.di"
        val repositoryPackage = "$packageName.repository"
        val servicePackage = "$packageName.service"
        val routePackage = "$packageName.route"
        val modeType = "${featureName}StorageMode"

        targetBundle.commonTarget?.let { commonTarget ->
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = dtoPackage,
                declarationName = modeType,
                kind = DeclarationKind.ENUM_CLASS,
                docComment = "$sceneTitle 的同步落盘策略。",
                enumEntries = listOf("LINKED", "COPIED", "VENDORED"),
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = dtoPackage,
                declarationName = "${featureName}Request",
                kind = DeclarationKind.DATA_CLASS,
                docComment = "$sceneTitle 保存请求。",
                constructorParams = listOf(
                    ctor(name = "name", type = "String", defaultValue = "\"\""),
                    ctor(name = "displayName", type = "String", defaultValue = "\"\""),
                    ctor(name = "primaryDir", type = "String", defaultValue = "\"\""),
                    ctor(name = "mirrorDirs", type = "List<String>", defaultValue = "emptyList()"),
                    ctor(name = "storageMode", type = modeType, defaultValue = "$modeType.LINKED"),
                ),
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = dtoPackage,
                declarationName = "${featureName}Response",
                kind = DeclarationKind.DATA_CLASS,
                docComment = "$sceneTitle 查询响应。",
                constructorParams = listOf(
                    ctor(name = "id", type = "String", defaultValue = "\"\""),
                    ctor(name = "name", type = "String", defaultValue = "\"\""),
                    ctor(name = "displayName", type = "String", defaultValue = "\"\""),
                    ctor(name = "primaryDir", type = "String", defaultValue = "\"\""),
                    ctor(name = "mirrorDirs", type = "List<String>", defaultValue = "emptyList()"),
                    ctor(name = "tags", type = "List<String>", defaultValue = "emptyList()"),
                    ctor(name = "storageMode", type = modeType, defaultValue = "$modeType.LINKED"),
                ),
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = dtoPackage,
                declarationName = "${featureName}Query",
                kind = DeclarationKind.DATA_CLASS,
                docComment = "$sceneTitle 查询条件。",
                constructorParams = listOf(
                    ctor(name = "keyword", type = "String", defaultValue = "\"\""),
                    ctor(name = "agentKey", type = "String?", nullable = true, defaultValue = "null"),
                    ctor(name = "tag", type = "String?", nullable = true, defaultValue = "null"),
                    ctor(name = "includeDotfiles", type = "Boolean", defaultValue = "true"),
                ),
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = dtoPackage,
                declarationName = "${featureName}PageRequest",
                kind = DeclarationKind.DATA_CLASS,
                docComment = "$sceneTitle 分页请求。",
                constructorParams = listOf(
                    ctor(name = "page", type = "Int", defaultValue = "1"),
                    ctor(name = "pageSize", type = "Int", defaultValue = "20"),
                ),
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = clientPackage,
                declarationName = "${featureName}ClientApi",
                kind = DeclarationKind.CLASS,
                docComment = "$sceneTitle Client API 壳。",
                imports = listOf(
                    import("io.insert-koin.core.annotation.Single"),
                    import("$dtoPackage.${featureName}PageRequest"),
                    import("$dtoPackage.${featureName}Query"),
                    import("$dtoPackage.${featureName}Request"),
                    import("$dtoPackage.${featureName}Response"),
                ),
                declarationAnnotations = listOf(annotation("Single")),
                functions = listOf(
                    fn(
                        name = "page",
                        returnType = "List<${featureName}Response>",
                        parameters = listOf(
                            FunctionParameterDto("pageRequest", "${featureName}PageRequest"),
                            FunctionParameterDto("query", "${featureName}Query"),
                        ),
                        bodyText = "TODO(\"扫描 skills / dotfiles 目录并返回分页结果\")",
                    ),
                    fn(
                        name = "get",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"读取 Skill / dotfile 详情\")",
                    ),
                    fn(
                        name = "create",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("request", "${featureName}Request")),
                        bodyText = "TODO(\"创建 Skill / dotfile 托管记录\")",
                    ),
                    fn(
                        name = "update",
                        returnType = "${featureName}Response",
                        parameters = listOf(
                            FunctionParameterDto("id", "String"),
                            FunctionParameterDto("request", "${featureName}Request"),
                        ),
                        bodyText = "TODO(\"更新 Skill / dotfile 托管记录\")",
                    ),
                    fn(
                        name = "deleteById",
                        returnType = "Unit",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"删除 Skill / dotfile 托管记录\")",
                    ),
                ),
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = statePackage,
                declarationName = "${featureName}ViewState",
                kind = DeclarationKind.CLASS,
                docComment = "$sceneTitle 工作台状态壳。",
                imports = listOf(
                    import("io.insert-koin.core.annotation.Single"),
                    import("$dtoPackage.${featureName}Request"),
                    import("$dtoPackage.${featureName}Response"),
                ),
                declarationAnnotations = listOf(annotation("Single")),
                properties = listOf(
                    prop(name = "keyword", type = "String", mutable = true, initializer = "\"\""),
                    prop(name = "items", type = "List<${featureName}Response>", mutable = true, initializer = "emptyList()"),
                    prop(name = "selectedDetail", type = "${featureName}Response?", mutable = true, nullable = true, initializer = "null"),
                    prop(name = "editingRequest", type = "${featureName}Request", mutable = true, initializer = "${featureName}Request()"),
                    prop(name = "includeDotfiles", type = "Boolean", mutable = true, initializer = "true"),
                    prop(name = "statusMessage", type = "String", mutable = true, initializer = "\"等待扫描 skill 仓库\""),
                ),
                functions = listOf(
                    fn(name = "refresh", returnType = "Unit", bodyText = "TODO(\"刷新 skills / dotfiles 列表\")"),
                    fn(
                        name = "loadDetail",
                        returnType = "Unit",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"读取 Skill / dotfile 详情\")",
                    ),
                    fn(
                        name = "submit",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("id", "String?", nullable = true, defaultValue = "null")),
                        bodyText = "TODO(\"提交 Skill / dotfile 变更\")",
                    ),
                ),
            )
            fileIds += createComposePageShell(
                target = commonTarget,
                packageName = screenPackage,
                declarationName = "${featureName}ListPage",
                statePackage = statePackage,
                stateType = "${featureName}ViewState",
                bodyText = "Text(\"${sceneTitle} 列表页壳，后续接入路径聚合、标签过滤和来源目录。\")",
            )
            fileIds += createComposePageShell(
                target = commonTarget,
                packageName = screenPackage,
                declarationName = "${featureName}DetailPage",
                statePackage = statePackage,
                stateType = "${featureName}ViewState",
                bodyText = "Text(\"${sceneTitle} 详情页壳，后续接入托管目录、镜像目录和同步状态。\")",
            )
            fileIds += createComposePageShell(
                target = commonTarget,
                packageName = screenPackage,
                declarationName = "${featureName}EditPage",
                statePackage = statePackage,
                stateType = "${featureName}ViewState",
                bodyText = "Text(\"${sceneTitle} 编辑页壳，后续接入本地目录、规则和同步策略表单。\")",
            )
            fileIds += createStructuredFile(
                target = commonTarget,
                packageName = diPackage,
                declarationName = "${featureName}CommonKoinModule",
                kind = DeclarationKind.CLASS,
                docComment = "把 $sceneTitle 的 commonMain 侧状态与页面壳纳入 Koin 聚合。",
                imports = listOf(
                    import("io.insert-koin.core.annotation.ComponentScan"),
                    import("io.insert-koin.core.annotation.Module"),
                ),
                declarationAnnotations = listOf(
                    annotation("Module"),
                    annotation("ComponentScan", arg("\"$packageName\"")),
                ),
            )
        }

        targetBundle.jvmTarget?.let { jvmTarget ->
            fileIds += createStructuredFile(
                target = jvmTarget,
                packageName = repositoryPackage,
                declarationName = "${featureName}Repository",
                kind = DeclarationKind.INTERFACE,
                docComment = "$sceneTitle Repository 壳。",
                imports = listOf(
                    import("$dtoPackage.${featureName}PageRequest"),
                    import("$dtoPackage.${featureName}Query"),
                    import("$dtoPackage.${featureName}Request"),
                    import("$dtoPackage.${featureName}Response"),
                ),
                functions = listOf(
                    interfaceFn(
                        name = "page",
                        returnType = "List<${featureName}Response>",
                        parameters = listOf(
                            FunctionParameterDto("pageRequest", "${featureName}PageRequest"),
                            FunctionParameterDto("query", "${featureName}Query"),
                        ),
                    ),
                    interfaceFn(
                        name = "get",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                    ),
                    interfaceFn(
                        name = "create",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("request", "${featureName}Request")),
                    ),
                    interfaceFn(
                        name = "update",
                        returnType = "${featureName}Response",
                        parameters = listOf(
                            FunctionParameterDto("id", "String"),
                            FunctionParameterDto("request", "${featureName}Request"),
                        ),
                    ),
                    interfaceFn(
                        name = "deleteById",
                        returnType = "Unit",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                    ),
                ),
            )
            fileIds += createStructuredFile(
                target = jvmTarget,
                packageName = servicePackage,
                declarationName = "${featureName}Service",
                kind = DeclarationKind.CLASS,
                docComment = "$sceneTitle Service 壳。",
                imports = listOf(
                    import("io.insert-koin.core.annotation.Single"),
                    import("$dtoPackage.${featureName}PageRequest"),
                    import("$dtoPackage.${featureName}Query"),
                    import("$dtoPackage.${featureName}Request"),
                    import("$dtoPackage.${featureName}Response"),
                ),
                declarationAnnotations = listOf(annotation("Single")),
                functions = listOf(
                    fn(
                        name = "page",
                        returnType = "List<${featureName}Response>",
                        parameters = listOf(
                            FunctionParameterDto("pageRequest", "${featureName}PageRequest"),
                            FunctionParameterDto("query", "${featureName}Query"),
                        ),
                        bodyText = "TODO(\"补齐 Skill / dotfile 分页规则\")",
                    ),
                    fn(
                        name = "get",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"补齐 Skill / dotfile 详情查询\")",
                    ),
                    fn(
                        name = "create",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("request", "${featureName}Request")),
                        bodyText = "TODO(\"补齐 Skill / dotfile 新增逻辑\")",
                    ),
                    fn(
                        name = "update",
                        returnType = "${featureName}Response",
                        parameters = listOf(
                            FunctionParameterDto("id", "String"),
                            FunctionParameterDto("request", "${featureName}Request"),
                        ),
                        bodyText = "TODO(\"补齐 Skill / dotfile 更新逻辑\")",
                    ),
                    fn(
                        name = "deleteById",
                        returnType = "Unit",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"补齐 Skill / dotfile 删除逻辑\")",
                    ),
                ),
            )
            fileIds += createStructuredFile(
                target = jvmTarget,
                packageName = routePackage,
                declarationName = "${featureName}RouteShell",
                kind = DeclarationKind.CLASS,
                docComment = buildString {
                    appendLine("$sceneTitle 路由壳。")
                    appendLine("POST /api/$routeSegment/page")
                    appendLine("GET /api/$routeSegment/{id}")
                    appendLine("POST /api/$routeSegment")
                    appendLine("PUT /api/$routeSegment/{id}")
                    append("DELETE /api/$routeSegment/{id}")
                },
                imports = listOf(
                    import("$dtoPackage.${featureName}PageRequest"),
                    import("$dtoPackage.${featureName}Query"),
                    import("$dtoPackage.${featureName}Request"),
                    import("$dtoPackage.${featureName}Response"),
                ),
                functions = listOf(
                    fn(
                        name = "page",
                        returnType = "List<${featureName}Response>",
                        parameters = listOf(
                            FunctionParameterDto("pageRequest", "${featureName}PageRequest"),
                            FunctionParameterDto("query", "${featureName}Query"),
                        ),
                        bodyText = "TODO(\"在顶层 spring2ktor route 中转调 ${featureName}Service.page\")",
                    ),
                    fn(
                        name = "get",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"在顶层 spring2ktor route 中转调 ${featureName}Service.get\")",
                    ),
                    fn(
                        name = "create",
                        returnType = "${featureName}Response",
                        parameters = listOf(FunctionParameterDto("request", "${featureName}Request")),
                        bodyText = "TODO(\"在顶层 spring2ktor route 中转调 ${featureName}Service.create\")",
                    ),
                    fn(
                        name = "update",
                        returnType = "${featureName}Response",
                        parameters = listOf(
                            FunctionParameterDto("id", "String"),
                            FunctionParameterDto("request", "${featureName}Request"),
                        ),
                        bodyText = "TODO(\"在顶层 spring2ktor route 中转调 ${featureName}Service.update\")",
                    ),
                    fn(
                        name = "deleteById",
                        returnType = "Unit",
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyText = "TODO(\"在顶层 spring2ktor route 中转调 ${featureName}Service.deleteById\")",
                    ),
                ),
            )
            fileIds += createStructuredFile(
                target = jvmTarget,
                packageName = diPackage,
                declarationName = "${featureName}ServerKoinModule",
                kind = DeclarationKind.CLASS,
                docComment = "把 $sceneTitle 的 jvmMain 侧 Repository、Service 和路由壳纳入 Koin 聚合。",
                imports = listOf(
                    import("io.insert-koin.core.annotation.ComponentScan"),
                    import("io.insert-koin.core.annotation.Module"),
                ),
                declarationAnnotations = listOf(
                    annotation("Module"),
                    annotation("ComponentScan", arg("\"$packageName\"")),
                ),
            )
        }
        return fileIds
    }

    private suspend fun createKcloudPluginScene(
        packageName: String,
        featureName: String,
        routeSegment: String,
        sceneTitle: String,
        targetBundle: SceneTargetBundle,
        notes: MutableList<String>,
    ): List<String> {
        val commonTarget = targetBundle.commonTarget
        if (commonTarget == null) {
            notes += "KCloud 插件场景要求有 commonMain 目标，当前未生成任何文件。"
            return emptyList()
        }
        notes += "KCloud 顶层 @Route 无参入口仍建议放插件模块的 screen 包顶层函数；当前先生成页面对象和路由常量。"
        val statePackage = "$packageName.state"
        val screenPackage = "$packageName.screen"
        val routePackage = "$packageName.route"
        val diPackage = "$packageName.di"
        val fileIds = mutableListOf<String>()
        fileIds += createStructuredFile(
            target = commonTarget,
            packageName = statePackage,
            declarationName = "${featureName}SceneState",
            kind = DeclarationKind.CLASS,
            docComment = "$sceneTitle 的插件工作台状态。",
            imports = listOf(import("io.insert-koin.core.annotation.Single")),
            declarationAnnotations = listOf(annotation("Single")),
            properties = listOf(
                prop(name = "currentRoute", type = "String", mutable = true, initializer = "\"$routeSegment/list\""),
                prop(name = "selectedId", type = "String?", mutable = true, nullable = true, initializer = "null"),
                prop(name = "statusMessage", type = "String", mutable = true, initializer = "\"等待接入 KCloud 宿主\""),
            ),
            functions = listOf(
                fn(
                    name = "openList",
                    returnType = "Unit",
                    bodyText = "currentRoute = \"${routeSegment}/list\"",
                ),
                fn(
                    name = "openDetail",
                    returnType = "Unit",
                    parameters = listOf(FunctionParameterDto("id", "String")),
                    bodyText = "selectedId = id; currentRoute = \"${routeSegment}/detail\"",
                ),
                fn(
                    name = "openEdit",
                    returnType = "Unit",
                    parameters = listOf(FunctionParameterDto("id", "String?", nullable = true, defaultValue = "null")),
                    bodyText = "selectedId = id; currentRoute = \"${routeSegment}/edit\"",
                ),
            ),
        )
        fileIds += createStructuredFile(
            target = commonTarget,
            packageName = routePackage,
            declarationName = "${featureName}KCloudRouteSpec",
            kind = DeclarationKind.OBJECT,
            docComment = "$sceneTitle 的 KCloud 场景常量。composeApp 只做宿主聚合，具体页面仍放插件模块 commonMain。",
            properties = listOf(
                prop(name = "sceneName", type = "String", initializer = "\"$sceneTitle\""),
                prop(name = "listRoute", type = "String", initializer = "\"$routeSegment/list\""),
                prop(name = "detailRoute", type = "String", initializer = "\"$routeSegment/detail\""),
                prop(name = "editRoute", type = "String", initializer = "\"$routeSegment/edit\""),
            ),
        )
        fileIds += createComposePageShell(
            target = commonTarget,
            packageName = screenPackage,
            declarationName = "${featureName}ListPage",
            statePackage = statePackage,
            stateType = "${featureName}SceneState",
            bodyText = "Text(\"${sceneTitle} 列表页壳，后续在插件模块顶层补 @Route 入口函数。\")",
        )
        fileIds += createComposePageShell(
            target = commonTarget,
            packageName = screenPackage,
            declarationName = "${featureName}DetailPage",
            statePackage = statePackage,
            stateType = "${featureName}SceneState",
            bodyText = "Text(\"${sceneTitle} 详情页壳，后续接入详情布局和右侧属性区。\")",
        )
        fileIds += createComposePageShell(
            target = commonTarget,
            packageName = screenPackage,
            declarationName = "${featureName}EditPage",
            statePackage = statePackage,
            stateType = "${featureName}SceneState",
            bodyText = "Text(\"${sceneTitle} 编辑页壳，后续接入插件表单与保存动作。\")",
        )
        fileIds += createStructuredFile(
            target = commonTarget,
            packageName = diPackage,
            declarationName = "${featureName}PluginKoinModule",
            kind = DeclarationKind.CLASS,
            docComment = "把 $sceneTitle 的插件状态和页面壳纳入 Koin 聚合。",
            imports = listOf(
                import("io.insert-koin.core.annotation.ComponentScan"),
                import("io.insert-koin.core.annotation.Module"),
            ),
            declarationAnnotations = listOf(
                annotation("Module"),
                annotation("ComponentScan", arg("\"$packageName\"")),
            ),
        )
        return fileIds
    }

    private suspend fun createComposePageShell(
        target: GenerationTargetDto,
        packageName: String,
        declarationName: String,
        statePackage: String,
        stateType: String,
        bodyText: String,
    ): String {
        return createStructuredFile(
            target = target,
            packageName = packageName,
            declarationName = declarationName,
            kind = DeclarationKind.OBJECT,
            docComment = "$declarationName Compose 页面壳。",
            imports = listOf(
                import("androidx.compose.material3.Text"),
                import("androidx.compose.runtime.Composable"),
                import("$statePackage.$stateType"),
            ),
            functions = listOf(
                fn(
                    name = "Content",
                    returnType = "Unit",
                    parameters = listOf(FunctionParameterDto("state", stateType)),
                    bodyText = bodyText,
                    annotations = listOf(annotation("Composable")),
                ),
            ),
        )
    }

    private suspend fun createStructuredFile(
        target: GenerationTargetDto,
        packageName: String,
        declarationName: String,
        kind: DeclarationKind,
        docComment: String? = null,
        imports: List<ImportSpec> = emptyList(),
        declarationAnnotations: List<AnnotationSpec> = emptyList(),
        constructorParams: List<ConstructorParamSpec> = emptyList(),
        properties: List<PropertySpec> = emptyList(),
        functions: List<FunctionSpec> = emptyList(),
        enumEntries: List<String> = emptyList(),
        superTypes: List<String> = emptyList(),
    ): String {
        val file = create(
            CreateSourceFileRequest(
                targetId = target.id,
                packageName = packageName,
                fileName = declarationName.ensureKtFileName(),
            ),
        )
        imports.forEach { spec ->
            createImport(CreateImportRequest(file.id, spec.path, spec.alias))
        }
        val declaration = declarationService.create(
            CreateDeclarationRequest(
                fileId = file.id,
                name = declarationName,
                kind = kind,
                superTypes = superTypes,
                docComment = docComment,
            ),
        )
        declarationAnnotations.forEach { annotation ->
            saveAnnotation(AnnotationOwnerType.DECLARATION, declaration.id, annotation)
        }
        constructorParams.forEach { spec ->
            declarationService.createConstructorParam(
                CreateConstructorParamRequest(
                    declarationId = declaration.id,
                    name = spec.name,
                    type = spec.type,
                    mutable = spec.mutable,
                    nullable = spec.nullable,
                    defaultValue = spec.defaultValue,
                ),
            )
        }
        properties.forEach { spec ->
            declarationService.createProperty(
                CreatePropertyRequest(
                    declarationId = declaration.id,
                    name = spec.name,
                    type = spec.type,
                    mutable = spec.mutable,
                    nullable = spec.nullable,
                    initializer = spec.initializer,
                    visibility = spec.visibility,
                    isOverride = spec.isOverride,
                ),
            )
        }
        enumEntries.forEach { entry ->
            declarationService.createEnumEntry(CreateEnumEntryRequest(declaration.id, entry))
        }
        functions.forEach { spec ->
            val function = declarationService.createFunctionStub(
                CreateFunctionStubRequest(
                    declarationId = declaration.id,
                    name = spec.name,
                    returnType = spec.returnType,
                    visibility = spec.visibility,
                    modifiers = spec.modifiers,
                    parameters = spec.parameters,
                    bodyMode = spec.bodyMode,
                    bodyText = spec.bodyText,
                ),
            )
            spec.annotations.forEach { annotation ->
                saveAnnotation(AnnotationOwnerType.FUNCTION, function.id, annotation)
            }
        }
        return file.id
    }

    private suspend fun saveAnnotation(
        ownerType: AnnotationOwnerType,
        ownerId: String,
        annotation: AnnotationSpec,
    ) {
        val saved = declarationService.createAnnotationUsage(
            CreateAnnotationUsageRequest(
                ownerType = ownerType,
                ownerId = ownerId,
                annotationClassName = annotation.name,
                useSiteTarget = annotation.useSiteTarget,
            ),
        )
        annotation.args.forEach { arg ->
            declarationService.createAnnotationArgument(
                CreateAnnotationArgumentRequest(
                    annotationUsageId = saved.id,
                    name = arg.name,
                    value = arg.value,
                ),
            )
        }
    }
}

private data class SceneTargetBundle(
    val primary: GenerationTargetDto,
    val commonTarget: GenerationTargetDto?,
    val jvmTarget: GenerationTargetDto?,
    val notes: List<String> = emptyList(),
)

private data class ImportSpec(
    val path: String,
    val alias: String? = null,
)

private data class AnnotationArgSpec(
    val name: String? = null,
    val value: String,
)

private data class AnnotationSpec(
    val name: String,
    val args: List<AnnotationArgSpec> = emptyList(),
    val useSiteTarget: String? = null,
)

private data class ConstructorParamSpec(
    val name: String,
    val type: String,
    val mutable: Boolean = false,
    val nullable: Boolean = false,
    val defaultValue: String? = null,
)

private data class PropertySpec(
    val name: String,
    val type: String,
    val mutable: Boolean = false,
    val nullable: Boolean = false,
    val initializer: String? = null,
    val visibility: CodeVisibility = CodeVisibility.PUBLIC,
    val isOverride: Boolean = false,
)

private data class FunctionSpec(
    val name: String,
    val returnType: String,
    val visibility: CodeVisibility = CodeVisibility.PUBLIC,
    val modifiers: List<String> = emptyList(),
    val parameters: List<FunctionParameterDto> = emptyList(),
    val bodyMode: FunctionBodyMode = FunctionBodyMode.TEMPLATE,
    val bodyText: String? = null,
    val annotations: List<AnnotationSpec> = emptyList(),
)

private fun import(path: String, alias: String? = null): ImportSpec = ImportSpec(path = path, alias = alias)

private fun arg(value: String, name: String? = null): AnnotationArgSpec = AnnotationArgSpec(name = name, value = value)

private fun annotation(
    name: String,
    vararg args: AnnotationArgSpec,
    useSiteTarget: String? = null,
): AnnotationSpec = AnnotationSpec(name = name, args = args.toList(), useSiteTarget = useSiteTarget)

private fun ctor(
    name: String,
    type: String,
    mutable: Boolean = false,
    nullable: Boolean = false,
    defaultValue: String? = null,
): ConstructorParamSpec = ConstructorParamSpec(
    name = name,
    type = type,
    mutable = mutable,
    nullable = nullable,
    defaultValue = defaultValue,
)

private fun prop(
    name: String,
    type: String,
    mutable: Boolean = false,
    nullable: Boolean = false,
    initializer: String? = null,
    visibility: CodeVisibility = CodeVisibility.PUBLIC,
    isOverride: Boolean = false,
): PropertySpec = PropertySpec(
    name = name,
    type = type,
    mutable = mutable,
    nullable = nullable,
    initializer = initializer,
    visibility = visibility,
    isOverride = isOverride,
)

private fun fn(
    name: String,
    returnType: String,
    visibility: CodeVisibility = CodeVisibility.PUBLIC,
    modifiers: List<String> = emptyList(),
    parameters: List<FunctionParameterDto> = emptyList(),
    bodyMode: FunctionBodyMode = FunctionBodyMode.TEMPLATE,
    bodyText: String? = null,
    annotations: List<AnnotationSpec> = emptyList(),
): FunctionSpec = FunctionSpec(
    name = name,
    returnType = returnType,
    visibility = visibility,
    modifiers = modifiers,
    parameters = parameters,
    bodyMode = bodyMode,
    bodyText = bodyText,
    annotations = annotations,
)

private fun interfaceFn(
    name: String,
    returnType: String,
    parameters: List<FunctionParameterDto> = emptyList(),
): FunctionSpec = FunctionSpec(
    name = name,
    returnType = returnType,
    parameters = parameters,
    bodyMode = FunctionBodyMode.TEMPLATE,
    bodyText = null,
)

private fun ScenePresetKind.displayName(): String {
    return when (this) {
        ScenePresetKind.BUSINESS_CRUD -> "业务 CRUD 壳"
        ScenePresetKind.SKILL_DOTFILE -> "Skills / Dotfiles 场景"
        ScenePresetKind.KCLOUD_PLUGIN -> "KCloud 插件场景"
    }
}
