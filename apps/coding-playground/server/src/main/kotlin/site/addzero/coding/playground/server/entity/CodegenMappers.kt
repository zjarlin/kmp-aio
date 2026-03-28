package site.addzero.coding.playground.server.entity

import site.addzero.coding.playground.shared.dto.*
import java.time.LocalDateTime

private fun LocalDateTime.asWire(): String = toString()

private inline fun <reified T : Enum<T>> enumValueOfOrDefault(value: String?, default: T): T {
    return value?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
}

fun CodegenProject.toDto(): CodegenProjectDto = CodegenProjectDto(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun GenerationTarget.toDto(): GenerationTargetDto = GenerationTargetDto(
    id = id,
    projectId = projectId,
    name = name,
    rootDir = rootDir,
    sourceSet = sourceSet,
    basePackage = basePackage,
    indexPackage = indexPackage,
    kspEnabled = kspEnabled,
    variables = decodeStringMap(variablesJson),
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun SourceFileMeta.toDto(): SourceFileMetaDto = SourceFileMetaDto(
    id = id,
    targetId = targetId,
    projectId = projectId,
    packageName = packageName,
    fileName = fileName,
    docComment = docComment,
    orderIndex = orderIndex,
    relativePath = "${packageName.replace('.', '/')}/$fileName",
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun DeclarationMeta.toDto(): DeclarationMetaDto = DeclarationMetaDto(
    id = id,
    fileId = fileId,
    targetId = targetId,
    packageName = packageName,
    fqName = fqName,
    name = name,
    kind = enumValueOfOrDefault(kind, DeclarationKind.OBJECT),
    visibility = enumValueOfOrDefault(visibility, CodeVisibility.PUBLIC),
    modifiers = decodeStringList(modifiersJson),
    superTypes = decodeStringList(superTypesJson),
    docComment = docComment,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun ConstructorParamMeta.toDto(): ConstructorParamMetaDto = ConstructorParamMetaDto(
    id = id,
    declarationId = declarationId,
    name = name,
    type = type,
    mutable = mutable,
    nullable = nullable,
    defaultValue = defaultValue,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun PropertyMeta.toDto(): PropertyMetaDto = PropertyMetaDto(
    id = id,
    declarationId = declarationId,
    name = name,
    type = type,
    mutable = mutable,
    nullable = nullable,
    initializer = initializer,
    visibility = enumValueOfOrDefault(visibility, CodeVisibility.PUBLIC),
    isOverride = isOverride,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun EnumEntryMeta.toDto(): EnumEntryMetaDto = EnumEntryMetaDto(
    id = id,
    declarationId = declarationId,
    name = name,
    arguments = decodeStringList(argumentsJson),
    bodyText = bodyText,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun AnnotationUsageMeta.toDto(): AnnotationUsageMetaDto = AnnotationUsageMetaDto(
    id = id,
    ownerType = enumValueOfOrDefault(ownerType, AnnotationOwnerType.DECLARATION),
    ownerId = ownerId,
    annotationClassName = annotationClassName,
    useSiteTarget = useSiteTarget,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun AnnotationArgumentMeta.toDto(): AnnotationArgumentMetaDto = AnnotationArgumentMetaDto(
    id = id,
    annotationUsageId = annotationUsageId,
    name = name,
    value = value,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun ImportMeta.toDto(): ImportMetaDto = ImportMetaDto(
    id = id,
    fileId = fileId,
    importPath = importPath,
    alias = alias,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun FunctionStubMeta.toDto(): FunctionStubMetaDto = FunctionStubMetaDto(
    id = id,
    declarationId = declarationId,
    name = name,
    returnType = returnType,
    visibility = enumValueOfOrDefault(visibility, CodeVisibility.PUBLIC),
    modifiers = decodeStringList(modifiersJson),
    parameters = decodeFunctionParameters(parametersJson),
    bodyMode = enumValueOfOrDefault(bodyMode, FunctionBodyMode.TEMPLATE),
    bodyText = bodyText,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun ManagedArtifactMeta.toDto(): ManagedArtifactMetaDto = ManagedArtifactMetaDto(
    id = id,
    projectId = projectId,
    targetId = targetId,
    fileId = fileId,
    declarationIds = decodeStringList(declarationIdsJson),
    absolutePath = absolutePath,
    markerText = markerText,
    metadataHash = metadataHash,
    sourceHash = sourceHash,
    contentHash = contentHash,
    syncStatus = enumValueOfOrDefault(syncStatus, ManagedArtifactSyncStatus.MISSING),
    lastExportedAt = lastExportedAt?.asWire(),
    lastImportedAt = lastImportedAt?.asWire(),
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun SyncConflictMeta.toDto(): SyncConflictMetaDto = SyncConflictMetaDto(
    id = id,
    projectId = projectId,
    targetId = targetId,
    fileId = fileId,
    artifactId = artifactId,
    reason = enumValueOfOrDefault(reason, ConflictReason.PARSE_FAILED),
    message = message,
    metadataHash = metadataHash,
    sourceHash = sourceHash,
    sourcePath = sourcePath,
    resolved = resolved,
    resolution = resolution?.let { enumValueOfOrDefault(it, SyncConflictResolution.METADATA_WINS) },
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)
