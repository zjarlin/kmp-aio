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

internal fun defaultKspTransportDefaults(): KspTransportDefaults =
    ModbusTransportDefaults(
        rtu =
            ModbusRtuTransportDefaults(
                portPath = "/dev/ttyUSB0",
                unitId = 1,
                baudRate = 9600,
                dataBits = 8,
                stopBits = 1,
                parity = "none",
                timeoutMs = 1_000,
                retries = 2,
            ),
        tcp =
            ModbusTcpTransportDefaults(
                host = "127.0.0.1",
                port = 502,
                unitId = 1,
                timeoutMs = 1_000,
                retries = 2,
            ),
        mqtt =
            ModbusMqttTransportDefaults(
                brokerUrl = "tcp://127.0.0.1:1883",
                clientId = "modbus-mqtt-client",
                requestTopic = "modbus/request",
                responseTopic = "modbus/response",
                qos = 1,
                timeoutMs = 1_000,
                retries = 2,
            ),
    ).toKspTransportDefaults()

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

internal fun cleanupGeneratedPackageDir(
    outputRoot: Path,
    relativePackagePath: String,
) {
    val packageDir = resolveWithinRoot(outputRoot, relativePackagePath)
    if (packageDir.exists()) {
        packageDir.toFile().deleteRecursively()
    }
}

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

internal fun ModbusTransportKind.toArtifactKspTransportKind(): KspTransportKind =
    when (this) {
        ModbusTransportKind.RTU -> KspTransportKind.RTU
        ModbusTransportKind.TCP -> KspTransportKind.TCP
        ModbusTransportKind.MQTT -> KspTransportKind.MQTT
    }

internal fun List<ModbusServiceModel>.toKspServiceModels(): List<KspServiceModel> =
    map(ModbusServiceModel::toKspServiceModel)

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

private fun ModbusWorkflowKind.toKspWorkflowKind(): KspWorkflowKind =
    when (this) {
        ModbusWorkflowKind.FLASH_FIRMWARE -> KspWorkflowKind.FLASH_FIRMWARE
    }

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

private fun ModbusReturnKind.toKspReturnKind(): KspReturnKind =
    when (this) {
        ModbusReturnKind.UNIT -> KspReturnKind.UNIT
        ModbusReturnKind.BOOLEAN -> KspReturnKind.BOOLEAN
        ModbusReturnKind.INT -> KspReturnKind.INT
        ModbusReturnKind.STRING -> KspReturnKind.STRING
        ModbusReturnKind.DTO -> KspReturnKind.DTO
        ModbusReturnKind.COMMAND_RESULT -> KspReturnKind.COMMAND_RESULT
    }

private fun ModbusPropertyModel.toKspPropertyModel(): KspPropertyModel =
    KspPropertyModel(
        name = name,
        qualifiedType = qualifiedType,
        valueKind = valueKind.toKspValueKind(),
        field = field?.toKspFieldModel(),
        doc = doc,
    )

private fun ModbusFieldModel.toKspFieldModel(): KspFieldModel =
    KspFieldModel(
        codecName = codecName,
        registerOffset = registerOffset,
        bitOffset = bitOffset,
        length = length,
        registerWidth = registerWidth,
    )

private fun ModbusValueKind.toKspValueKind(): KspValueKind =
    when (this) {
        ModbusValueKind.BOOLEAN -> KspValueKind.BOOLEAN
        ModbusValueKind.INT -> KspValueKind.INT
        ModbusValueKind.BYTES -> KspValueKind.BYTES
        ModbusValueKind.STRING -> KspValueKind.STRING
    }

private fun ModbusDocModel.toKspDocModel(): KspDocModel =
    KspDocModel(
        summary = summary,
        descriptionLines = descriptionLines,
        parameterDocs = parameterDocs,
    )
