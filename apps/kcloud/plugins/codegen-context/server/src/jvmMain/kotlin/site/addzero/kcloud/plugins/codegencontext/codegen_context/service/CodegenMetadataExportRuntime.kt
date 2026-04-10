package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.util.ServiceLoader
import site.addzero.device.protocol.modbus.ksp.core.ModbusKspOptions
import site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncContext
import site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncTool
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportKind as KspTransportKind
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportSettingsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataFirmwareSyncDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataIssueDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenProjectSyncResultDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataIssueSeverity
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataTransportKind

internal data class MemoryKspLogEntry(
    val severity: CodegenMetadataIssueSeverity,
    val message: String,
)

internal class MemoryKspLogger : KSPLogger {
    private val entries = mutableListOf<MemoryKspLogEntry>()

    fun size(): Int = entries.size

    fun slice(fromIndex: Int): List<MemoryKspLogEntry> = entries.drop(fromIndex)

    override fun logging(
        message: String,
        symbol: KSNode?,
    ) {
        entries += MemoryKspLogEntry(CodegenMetadataIssueSeverity.INFO, message)
    }

    override fun info(
        message: String,
        symbol: KSNode?,
    ) {
        entries += MemoryKspLogEntry(CodegenMetadataIssueSeverity.INFO, message)
    }

    override fun warn(
        message: String,
        symbol: KSNode?,
    ) {
        entries += MemoryKspLogEntry(CodegenMetadataIssueSeverity.WARNING, message)
    }

    override fun error(
        message: String,
        symbol: KSNode?,
    ) {
        entries += MemoryKspLogEntry(CodegenMetadataIssueSeverity.ERROR, message)
    }

    override fun exception(e: Throwable) {
        entries += MemoryKspLogEntry(CodegenMetadataIssueSeverity.ERROR, e.message ?: e::class.simpleName.orEmpty())
    }
}

private class NoopCodeGenerator : CodeGenerator {
    override val generatedFile: Collection<File> = emptyList()

    override fun createNewFile(
        dependencies: Dependencies,
        packageName: String,
        fileName: String,
        extensionName: String,
    ): OutputStream = ByteArrayOutputStream()

    override fun createNewFileByPath(
        dependencies: Dependencies,
        path: String,
        extensionName: String,
    ): OutputStream = ByteArrayOutputStream()

    override fun associate(
        sources: List<KSFile>,
        packageName: String,
        fileName: String,
        extensionName: String,
    ) = Unit

    override fun associateByPath(
        sources: List<KSFile>,
        path: String,
        extensionName: String,
    ) = Unit

    override fun associateWithClasses(
        classes: List<KSClassDeclaration>,
        packageName: String,
        fileName: String,
        extensionName: String,
    ) = Unit

    override fun associateWithFunctions(
        functions: List<KSFunctionDeclaration>,
        packageName: String,
        fileName: String,
        extensionName: String,
    ) = Unit

    override fun associateWithProperties(
        properties: List<KSPropertyDeclaration>,
        packageName: String,
        fileName: String,
        extensionName: String,
    ) = Unit
}

internal fun createMetadataExportEnvironment(
    exportSettings: CodegenMetadataExportSettingsDto,
    transport: KspTransportKind,
    logger: MemoryKspLogger = MemoryKspLogger(),
): SymbolProcessorEnvironment =
    SymbolProcessorEnvironment(
        options = buildMetadataExportOptions(exportSettings, transport),
        kotlinVersion = KotlinVersion.CURRENT,
        codeGenerator = NoopCodeGenerator(),
        logger = logger,
    )

internal fun MemoryKspLogger.toIssues(
    transport: CodegenMetadataTransportKind,
    locationPrefix: String,
    fromIndex: Int = 0,
    includeInfo: Boolean = false,
): List<CodegenMetadataIssueDto> =
    slice(fromIndex)
        .filter { entry -> includeInfo || entry.severity != CodegenMetadataIssueSeverity.INFO }
        .map { entry ->
            CodegenMetadataIssueDto(
                severity = entry.severity,
                location = "$locationPrefix.${transport.name.lowercase()}",
                message = entry.message,
            )
        }

internal fun runProjectSyncTools(
    environment: SymbolProcessorEnvironment,
    transport: KspTransportKind,
    firmwareSync: CodegenMetadataFirmwareSyncDto,
    externalSourceFiles: List<File>,
    logger: MemoryKspLogger,
): List<CodegenProjectSyncResultDto> {
    if (externalSourceFiles.isEmpty()) {
        return emptyList()
    }
    val context =
        ModbusProjectSyncContext(
            environment = environment,
            transport = transport,
            externalSourceFiles = externalSourceFiles.distinctBy(File::getAbsolutePath),
        )
    val projectDir = File(firmwareSync.cOutputProjectDir).absoluteFile
    return ServiceLoader
        .load(ModbusProjectSyncTool::class.java, ModbusProjectSyncTool::class.java.classLoader)
        .toList()
        .filter { tool -> tool.isEnabled(context) }
        .map { tool ->
            val trackedFile = resolveTrackedSyncFile(tool.toolId, projectDir, firmwareSync)
            val before = trackedFile?.takeIf(File::isFile)?.readText()
            val logStart = logger.size()
            runCatching {
                tool.sync(context)
            }.onFailure { throwable ->
                logger.exception(throwable)
            }
            val after = trackedFile?.takeIf(File::isFile)?.readText()
            val messages = logger.slice(logStart)
            val updated =
                when {
                    trackedFile == null -> messages.any { entry -> entry.severity != CodegenMetadataIssueSeverity.ERROR }
                    before == null && trackedFile.isFile -> true
                    before != after -> true
                    else -> false
                }
            CodegenProjectSyncResultDto(
                toolId = tool.toolId,
                transport = transport.toMetadataTransportKind(),
                updated = updated,
                filePath = trackedFile?.absolutePath,
                message = messages.joinToString(" | ") { entry -> entry.message }.ifBlank { "未检测到工程文件变更。" },
            )
        }
}

private fun buildMetadataExportOptions(
    exportSettings: CodegenMetadataExportSettingsDto,
    transport: KspTransportKind,
): Map<String, String> =
    buildMap {
        put(ModbusKspOptions.TRANSPORTS_OPTION, transport.transportId)
        put("addzero.modbus.c.output.projectDir", exportSettings.firmwareSync.cOutputProjectDir)
        exportSettings.firmwareSync.bridgeImplPath.cleanNullable()?.let { value ->
            put("addzero.modbus.c.bridgeImpl.path", value)
        }
        exportSettings.firmwareSync.keilUvprojxPath.cleanNullable()?.let { value ->
            put("addzero.modbus.keil.uvprojx.path", value)
        }
        exportSettings.firmwareSync.keilTargetName.cleanNullable()?.let { value ->
            put("addzero.modbus.keil.targetName", value)
        }
        exportSettings.firmwareSync.keilGroupName.cleanNullable()?.let { value ->
            put("addzero.modbus.keil.groupName", value)
        }
        exportSettings.firmwareSync.mxprojectPath.cleanNullable()?.let { value ->
            put("addzero.modbus.mxproject.path", value)
        }
        put(ModbusKspOptions.RTU_PORT_PATH_OPTION, exportSettings.rtuDefaults.portPath)
        put(ModbusKspOptions.RTU_UNIT_ID_OPTION, exportSettings.rtuDefaults.unitId)
        put(ModbusKspOptions.RTU_BAUD_RATE_OPTION, exportSettings.rtuDefaults.baudRate)
        put(ModbusKspOptions.RTU_DATA_BITS_OPTION, exportSettings.rtuDefaults.dataBits)
        put(ModbusKspOptions.RTU_STOP_BITS_OPTION, exportSettings.rtuDefaults.stopBits)
        put(ModbusKspOptions.RTU_PARITY_OPTION, exportSettings.rtuDefaults.parity)
        put(ModbusKspOptions.RTU_TIMEOUT_MS_OPTION, exportSettings.rtuDefaults.timeoutMs)
        put(ModbusKspOptions.RTU_RETRIES_OPTION, exportSettings.rtuDefaults.retries)
        put(ModbusKspOptions.TCP_HOST_OPTION, exportSettings.tcpDefaults.host)
        put(ModbusKspOptions.TCP_PORT_OPTION, exportSettings.tcpDefaults.port)
        put(ModbusKspOptions.TCP_UNIT_ID_OPTION, exportSettings.tcpDefaults.unitId)
        put(ModbusKspOptions.TCP_TIMEOUT_MS_OPTION, exportSettings.tcpDefaults.timeoutMs)
        put(ModbusKspOptions.TCP_RETRIES_OPTION, exportSettings.tcpDefaults.retries)
        put(ModbusKspOptions.MQTT_BROKER_URL_OPTION, exportSettings.mqttDefaults.brokerUrl)
        put(ModbusKspOptions.MQTT_CLIENT_ID_OPTION, exportSettings.mqttDefaults.clientId)
        put(ModbusKspOptions.MQTT_REQUEST_TOPIC_OPTION, exportSettings.mqttDefaults.requestTopic)
        put(ModbusKspOptions.MQTT_RESPONSE_TOPIC_OPTION, exportSettings.mqttDefaults.responseTopic)
        put(ModbusKspOptions.MQTT_QOS_OPTION, exportSettings.mqttDefaults.qos)
        put(ModbusKspOptions.MQTT_TIMEOUT_MS_OPTION, exportSettings.mqttDefaults.timeoutMs)
        put(ModbusKspOptions.MQTT_RETRIES_OPTION, exportSettings.mqttDefaults.retries)
    }

private fun resolveTrackedSyncFile(
    toolId: String,
    projectDir: File,
    firmwareSync: CodegenMetadataFirmwareSyncDto,
): File? =
    when (toolId) {
        "keil-uvprojx" -> firmwareSync.keilUvprojxPath.cleanNullable()?.let { path -> resolveConfiguredFile(projectDir, path) }
        "stm32cubemx-mxproject" ->
            firmwareSync.mxprojectPath.cleanNullable()
                ?.let { path -> resolveConfiguredFile(projectDir, path) }
                ?: projectDir.resolve(".mxproject")

        else -> null
    }

private fun resolveConfiguredFile(
    projectDir: File,
    rawPath: String,
): File {
    val configured = File(rawPath)
    return if (configured.isAbsolute) {
        configured
    } else {
        projectDir.resolve(rawPath).absoluteFile
    }
}

private fun KspTransportKind.toMetadataTransportKind(): CodegenMetadataTransportKind =
    when (this) {
        KspTransportKind.RTU -> CodegenMetadataTransportKind.RTU
        KspTransportKind.TCP -> CodegenMetadataTransportKind.TCP
        KspTransportKind.MQTT -> CodegenMetadataTransportKind.MQTT
    }
