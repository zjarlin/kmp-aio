package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenGenerationSettingsDto
import site.addzero.device.protocol.modbus.codegen.model.ModbusDocModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusFieldModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusMqttTransportDefaults
import site.addzero.device.protocol.modbus.codegen.model.ModbusOperationModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusParameterModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusPropertyModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusReturnKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusReturnTypeModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusRtuTransportDefaults
import site.addzero.device.protocol.modbus.codegen.model.ModbusServiceModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusTcpTransportDefaults
import site.addzero.device.protocol.modbus.codegen.model.ModbusTransportDefaults
import site.addzero.device.protocol.modbus.codegen.model.ModbusTransportKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusValueKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusWorkflowKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusWorkflowModel
import site.addzero.device.protocol.modbus.ksp.core.GeneratedArtifact as KspGeneratedArtifact
import site.addzero.device.protocol.modbus.ksp.core.ModbusDocModel as KspDocModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusFieldModel as KspFieldModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusMqttTransportDefaults as KspModbusMqttTransportDefaults
import site.addzero.device.protocol.modbus.ksp.core.ModbusOperationModel as KspOperationModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusParameterModel as KspParameterModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusPropertyModel as KspPropertyModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusReturnKind as KspReturnKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusReturnTypeModel as KspReturnTypeModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusRtuTransportDefaults as KspModbusRtuTransportDefaults
import site.addzero.device.protocol.modbus.ksp.core.ModbusServiceModel as KspServiceModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusTcpTransportDefaults as KspModbusTcpTransportDefaults
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportDefaults as KspTransportDefaults
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportKind as KspTransportKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusValueKind as KspValueKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusWorkflowKind as KspWorkflowKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusWorkflowModel as KspWorkflowModel

private const val MCU_DEVICE_PACKAGE = "site.addzero.kcloud.plugins.mcuconsole.modbus.device"

/**
 * 表示resolved生成设置。
 *
 * @property workspaceRoot workspace根目录。
 * @property serverOutputRoot 服务端输出根目录。
 * @property sharedOutputRoot 共享输出根目录。
 * @property serverPackageDir 服务端包dir。
 * @property sharedPackageDir 共享包dir。
 * @property gatewayOutputRoot 网关输出根目录。
 * @property apiClientOutputRoot API 客户端输出根目录。
 * @property apiClientPackageName API 客户端包名。
 * @property springRouteOutputRoot Spring 路由输出根目录。
 * @property cOutputRoot C 输出根目录。
 * @property markdownOutputRoot Markdown 输出根目录。
 * @property transportDefaults 传输默认。
 */
internal data class ResolvedGenerationSettings(
    val workspaceRoot: Path,
    val serverOutputRoot: Path,
    val sharedOutputRoot: Path,
    val serverPackageDir: Path,
    val sharedPackageDir: Path,
    val gatewayOutputRoot: Path,
    val apiClientOutputRoot: Path?,
    val apiClientPackageName: String?,
    val springRouteOutputRoot: Path?,
    val cOutputRoot: Path?,
    val markdownOutputRoot: Path?,
    val transportDefaults: KspTransportDefaults,
)

/**
 * 处理locateworkspace根目录。
 */
internal fun locateWorkspaceRoot(): Path {
    val overrideRoot = System.getProperty("codegen.context.repoRoot")?.takeIf(String::isNotBlank)
    var current =
        Path.of(overrideRoot ?: System.getProperty("user.dir"))
            .toAbsolutePath()
            .normalize()
    while (true) {
        if (current.resolve("settings.gradle.kts").exists() &&
            current.resolve("apps/kcloud/plugins/mcu-console").exists()
        ) {
            return current
        }
        current = current.parent ?: break
    }
    error("Unable to locate the kmp-aio workspace root.")
}

/**
 * 处理代码生成上下文详情数据传输对象。
 */
internal fun CodegenContextDetailDto.resolveGenerationSettings(): ResolvedGenerationSettings {
    val workspaceRoot = locateWorkspaceRoot()
    val settings = generationSettings
    val serverOutputRoot =
        settings.serverOutputRoot
            ?.toAbsoluteNormalizedPath()
            ?: workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/server/generated/jvmMain/kotlin").normalize()
    val sharedOutputRoot =
        settings.sharedOutputRoot
            ?.toAbsoluteNormalizedPath()
            ?: workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/shared/generated/commonMain/kotlin").normalize()
    val gatewayOutputRoot =
        settings.gatewayOutputRoot
            ?.toAbsoluteNormalizedPath()
            ?: serverOutputRoot
    val legacyExternalRoot = externalCOutputRoot?.toAbsoluteNormalizedPath()
    val cOutputRoot = settings.cOutputRoot?.toAbsoluteNormalizedPath() ?: legacyExternalRoot?.resolve("c")?.normalize()
    val markdownOutputRoot =
        settings.markdownOutputRoot?.toAbsoluteNormalizedPath()
            ?: legacyExternalRoot?.resolve("markdown")?.normalize()
    return ResolvedGenerationSettings(
        workspaceRoot = workspaceRoot,
        serverOutputRoot = serverOutputRoot,
        sharedOutputRoot = sharedOutputRoot,
        serverPackageDir = resolveWithinRoot(serverOutputRoot, MCU_DEVICE_PACKAGE.replace('.', '/')),
        sharedPackageDir = resolveWithinRoot(sharedOutputRoot, MCU_DEVICE_PACKAGE.replace('.', '/')),
        gatewayOutputRoot = gatewayOutputRoot,
        apiClientOutputRoot = settings.apiClientOutputRoot?.toAbsoluteNormalizedPath(),
        apiClientPackageName = settings.apiClientPackageName,
        springRouteOutputRoot = settings.springRouteOutputRoot?.toAbsoluteNormalizedPath(),
        cOutputRoot = cOutputRoot,
        markdownOutputRoot = markdownOutputRoot,
        transportDefaults = settings.toKspTransportDefaults(),
    )
}

/**
 * 处理默认ksp传输默认。
 */
internal fun defaultKspTransportDefaults(): KspTransportDefaults =
    CodegenGenerationSettingsDto().toKspTransportDefaults()

/**
 * 处理代码生成设置数据传输对象。
 */
internal fun CodegenGenerationSettingsDto.toKspTransportDefaults(): KspTransportDefaults =
    ModbusTransportDefaults(
        rtu =
            ModbusRtuTransportDefaults(
                portPath = rtuDefaults.portPath,
                unitId = rtuDefaults.unitId,
                baudRate = rtuDefaults.baudRate,
                dataBits = rtuDefaults.dataBits,
                stopBits = rtuDefaults.stopBits,
                parity = rtuDefaults.parity,
                timeoutMs = rtuDefaults.timeoutMs,
                retries = rtuDefaults.retries,
            ),
        tcp =
            ModbusTcpTransportDefaults(
                host = tcpDefaults.host,
                port = tcpDefaults.port,
                unitId = tcpDefaults.unitId,
                timeoutMs = tcpDefaults.timeoutMs,
                retries = tcpDefaults.retries,
            ),
        mqtt =
            ModbusMqttTransportDefaults(
                brokerUrl = mqttDefaults.brokerUrl,
                clientId = mqttDefaults.clientId,
                requestTopic = mqttDefaults.requestTopic,
                responseTopic = mqttDefaults.responseTopic,
                qos = mqttDefaults.qos,
                timeoutMs = mqttDefaults.timeoutMs,
                retries = mqttDefaults.retries,
            ),
    ).toKspTransportDefaults()

/**
 * 写入ksp产物with路径。
 *
 * @param outputRoot 输出根目录。
 * @param artifacts artifacts。
 */
internal fun writeKspArtifactsWithPaths(
    outputRoot: Path,
    artifacts: List<KspGeneratedArtifact>,
): List<String> =
    artifacts.map { artifact ->
        val relativePath =
            buildString {
                artifact.packageName
                    ?.takeIf(String::isNotBlank)
                    ?.let { packageName ->
                        append(packageName.replace('.', '/'))
                        append('/')
                    }
                append(artifact.fileName)
                append('.')
                append(artifact.extensionName)
            }
        val targetFile = resolveWithinRoot(outputRoot, relativePath)
        val content =
            if (artifact.extensionName == "kt") {
                artifact.content.normalizeGeneratedKotlin()
            } else {
                artifact.content
            }
        writeTextFile(targetFile, content)
        targetFile.toAbsolutePath().normalize().toString()
    }

/**
 * 处理cleanup生成包dir。
 *
 * @param outputRoot 输出根目录。
 * @param relativePackagePath relative包路径。
 */
internal fun cleanupGeneratedPackageDir(
    outputRoot: Path,
    relativePackagePath: String,
) {
    val packageDir = resolveWithinRoot(outputRoot, relativePackagePath)
    if (packageDir.exists()) {
        packageDir.toFile().deleteRecursively()
    }
}

/**
 * 处理cleanup生成Markdown产物。
 *
 * @param outputRoot 输出根目录。
 * @param transport 传输。
 */
internal fun cleanupGeneratedMarkdownArtifacts(
    outputRoot: Path,
    transport: ModbusTransportKind,
) {
    val markdownDir = resolveWithinRoot(outputRoot, "generated/modbus/protocols")
    if (!markdownDir.exists()) {
        return
    }
    markdownDir
        .listDirectoryEntries("*.md")
        .filter { file ->
            file.isRegularFile() && file.name.endsWith(".${transport.transportId}.protocol.md")
        }.forEach { file ->
            file.deleteIfExists()
        }
}

/**
 * 解析within根目录。
 *
 * @param root 根目录。
 * @param relative relative。
 */
internal fun resolveWithinRoot(
    root: Path,
    relative: String,
): Path {
    val normalizedRoot = root.toAbsolutePath().normalize()
    val target = normalizedRoot.resolve(relative).normalize()
    check(target.startsWith(normalizedRoot)) {
        "Refuse to write outside generated roots: $target"
    }
    return target
}

/**
 * 处理string。
 */
private fun String.toAbsoluteNormalizedPath(): Path =
    Path.of(this).toAbsolutePath().normalize()

/**
 * 处理modbus传输类型。
 */
internal fun ModbusTransportKind.toArtifactKspTransportKind(): KspTransportKind =
    when (this) {
        ModbusTransportKind.RTU -> KspTransportKind.RTU
        ModbusTransportKind.TCP -> KspTransportKind.TCP
        ModbusTransportKind.MQTT -> KspTransportKind.MQTT
    }

/**
 * 处理列表。
 */
internal fun List<ModbusServiceModel>.toKspServiceModels(): List<KspServiceModel> =
    map(ModbusServiceModel::toKspServiceModel)

/**
 * 写入textfile。
 *
 * @param target 目标。
 * @param content content。
 */
private fun writeTextFile(
    target: Path,
    content: String,
) {
    target.parent?.createDirectories()
    Files.writeString(
        target,
        content,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE,
    )
}

/**
 * 处理modbus传输默认。
 */
private fun ModbusTransportDefaults.toKspTransportDefaults(): KspTransportDefaults =
    KspTransportDefaults(
        rtu =
            KspModbusRtuTransportDefaults(
                portPath = rtu.portPath,
                unitId = rtu.unitId,
                baudRate = rtu.baudRate,
                dataBits = rtu.dataBits,
                stopBits = rtu.stopBits,
                parity = rtu.parity,
                timeoutMs = rtu.timeoutMs,
                retries = rtu.retries,
            ),
        tcp =
            KspModbusTcpTransportDefaults(
                host = tcp.host,
                port = tcp.port,
                unitId = tcp.unitId,
                timeoutMs = tcp.timeoutMs,
                retries = tcp.retries,
            ),
        mqtt =
            KspModbusMqttTransportDefaults(
                brokerUrl = mqtt.brokerUrl,
                clientId = mqtt.clientId,
                requestTopic = mqtt.requestTopic,
                responseTopic = mqtt.responseTopic,
                qos = mqtt.qos,
                timeoutMs = mqtt.timeoutMs,
                retries = mqtt.retries,
            ),
    )

/**
 * 处理modbus服务模型。
 */
private fun ModbusServiceModel.toKspServiceModel(): KspServiceModel =
    KspServiceModel(
        interfacePackage = interfacePackage,
        interfaceSimpleName = interfaceSimpleName,
        interfaceQualifiedName = interfaceQualifiedName,
        serviceId = serviceId,
        summary = summary,
        basePath = basePath,
        transport = transport.toArtifactKspTransportKind(),
        doc = doc.toKspDocModel(),
        operations = operations.map(ModbusOperationModel::toKspOperationModel),
        workflows = workflows.map(ModbusWorkflowModel::toKspWorkflowModel),
    )

/**
 * 处理modbusworkflow模型。
 */
private fun ModbusWorkflowModel.toKspWorkflowModel(): KspWorkflowModel =
    KspWorkflowModel(
        kind = kind.toKspWorkflowKind(),
        methodName = methodName,
        workflowId = workflowId,
        requestClassName = requestClassName,
        requestQualifiedName = requestQualifiedName,
        bytesParameterName = bytesParameterName,
        returnType = returnType.toKspReturnTypeModel(),
        doc = doc.toKspDocModel(),
        startMethodName = startMethodName,
        chunkMethodName = chunkMethodName,
        commitMethodName = commitMethodName,
        resetMethodName = resetMethodName,
    )

/**
 * 处理modbusworkflow类型。
 */
private fun ModbusWorkflowKind.toKspWorkflowKind(): KspWorkflowKind =
    when (this) {
        ModbusWorkflowKind.FLASH_FIRMWARE -> KspWorkflowKind.FLASH_FIRMWARE
    }

/**
 * 处理modbusoperation模型。
 */
private fun ModbusOperationModel.toKspOperationModel(): KspOperationModel =
    KspOperationModel(
        methodName = methodName,
        operationId = operationId,
        functionCodeName = functionCodeName,
        address = address,
        quantity = quantity,
        requestClassName = requestClassName,
        requestQualifiedName = requestQualifiedName,
        parameters = parameters.map(ModbusParameterModel::toKspParameterModel),
        returnType = returnType.toKspReturnTypeModel(),
        doc = doc.toKspDocModel(),
    )

/**
 * 处理modbusparameter模型。
 */
private fun ModbusParameterModel.toKspParameterModel(): KspParameterModel =
    KspParameterModel(
        name = name,
        qualifiedType = qualifiedType,
        valueKind = valueKind.toKspValueKind(),
        order = order,
        codecName = codecName,
        registerOffset = registerOffset,
        bitOffset = bitOffset,
        registerWidth = registerWidth,
        length = length,
        doc = doc,
    )

/**
 * 处理modbusreturn类型模型。
 */
private fun ModbusReturnTypeModel.toKspReturnTypeModel(): KspReturnTypeModel =
    KspReturnTypeModel(
        qualifiedName = qualifiedName,
        simpleName = simpleName,
        kind = kind.toKspReturnKind(),
        docSummary = docSummary,
        valueKind = valueKind?.toKspValueKind(),
        codecName = codecName,
        length = length,
        registerWidth = registerWidth,
        properties = properties.map(ModbusPropertyModel::toKspPropertyModel),
    )

/**
 * 处理modbusreturn类型。
 */
private fun ModbusReturnKind.toKspReturnKind(): KspReturnKind =
    when (this) {
        ModbusReturnKind.UNIT -> KspReturnKind.UNIT
        ModbusReturnKind.BOOLEAN -> KspReturnKind.BOOLEAN
        ModbusReturnKind.INT -> KspReturnKind.INT
        ModbusReturnKind.STRING -> KspReturnKind.STRING
        ModbusReturnKind.DTO -> KspReturnKind.DTO
        ModbusReturnKind.COMMAND_RESULT -> KspReturnKind.COMMAND_RESULT
    }

/**
 * 处理modbus属性模型。
 */
private fun ModbusPropertyModel.toKspPropertyModel(): KspPropertyModel =
    KspPropertyModel(
        name = name,
        qualifiedType = qualifiedType,
        valueKind = valueKind.toKspValueKind(),
        field = field?.toKspFieldModel(),
        doc = doc,
    )

/**
 * 处理modbus字段模型。
 */
private fun ModbusFieldModel.toKspFieldModel(): KspFieldModel =
    KspFieldModel(
        codecName = codecName,
        registerOffset = registerOffset,
        bitOffset = bitOffset,
        length = length,
        registerWidth = registerWidth,
    )

/**
 * 处理modbus值类型。
 */
private fun ModbusValueKind.toKspValueKind(): KspValueKind =
    when (this) {
        ModbusValueKind.BOOLEAN -> KspValueKind.BOOLEAN
        ModbusValueKind.INT -> KspValueKind.INT
        ModbusValueKind.BYTES -> KspValueKind.BYTES
        ModbusValueKind.STRING -> KspValueKind.STRING
    }

/**
 * 处理modbusdoc模型。
 */
private fun ModbusDocModel.toKspDocModel(): KspDocModel =
    KspDocModel(
        summary = summary,
        descriptionLines = descriptionLines,
        parameterDocs = parameterDocs,
    )
