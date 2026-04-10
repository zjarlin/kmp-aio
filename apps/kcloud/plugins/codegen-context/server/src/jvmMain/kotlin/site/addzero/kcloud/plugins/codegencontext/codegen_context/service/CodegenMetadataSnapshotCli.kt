package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Path
import java.sql.DriverManager
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import site.addzero.device.protocol.modbus.codegen.ModbusContractDefaultsResolver
import site.addzero.device.protocol.modbus.codegen.ModbusMetadataJsonCodec
import site.addzero.device.protocol.modbus.codegen.registerWidth
import site.addzero.device.protocol.modbus.codegen.model.ModbusDocModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusFieldModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusOperationModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusParameterModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusPropertyModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusReturnKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusReturnTypeModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusServiceModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusTransportKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusValueKind

internal const val DEFAULT_CODEGEN_CONTEXT_SQLITE_SNAPSHOT_RELATIVE_PATH =
    "src/jvmMain/resources/snapshots/codegen-context-metadata.sqlite"

private const val MCU_CONSOLE_CONTEXT_ID = 1L
private const val MCU_CONSOLE_CONTEXT_CODE = "MCU_DEVICE_DEFAULT"
private const val MCU_CONSOLE_CONTEXT_NAME = "MCU 默认协议桥接"
private const val MCU_CONSOLE_PROTOCOL_TEMPLATE_CODE = "MODBUS_RTU_CLIENT"
private const val MCU_DEVICE_PACKAGE = "site.addzero.kcloud.plugins.mcuconsole.modbus.device"
private const val SNAPSHOT_UPDATED_AT = 1775692800000L

fun main(args: Array<String>) {
    val output =
        when {
            args.isEmpty() -> Path.of(DEFAULT_CODEGEN_CONTEXT_SQLITE_SNAPSHOT_RELATIVE_PATH)
            args.size == 2 && args[0] == "--output" -> Path.of(args[1])
            else -> error("Unsupported arguments: ${args.joinToString(" ")}")
        }
    writeDefaultCodegenContextSqliteSnapshot(output)
    println("codegen-context sqlite snapshot written to ${output.toAbsolutePath().normalize()}")
}

internal fun writeDefaultCodegenContextSqliteSnapshot(
    output: Path,
) {
    output.parent?.createDirectories()
    output.deleteIfExists()
    Class.forName("org.sqlite.JDBC")
    DriverManager.getConnection("jdbc:sqlite:${output.toAbsolutePath().normalize()}").use { connection ->
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = OFF")
            statement.execute(
                """
                CREATE TABLE codegen_context_modbus_contract (
                    context_id INTEGER NOT NULL,
                    context_code TEXT NOT NULL,
                    context_name TEXT NOT NULL,
                    enabled INTEGER NOT NULL,
                    consumer_target TEXT NOT NULL,
                    protocol_template_code TEXT NOT NULL,
                    transport TEXT NOT NULL,
                    selected INTEGER NOT NULL DEFAULT 0,
                    payload TEXT NOT NULL,
                    updated_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            statement.execute(
                """
                CREATE INDEX idx_codegen_context_modbus_contract_selected
                ON codegen_context_modbus_contract (consumer_target, transport, selected, updated_at)
                """.trimIndent(),
            )
        }
        connection.prepareStatement(
            """
            INSERT INTO codegen_context_modbus_contract (
                context_id,
                context_code,
                context_name,
                enabled,
                consumer_target,
                protocol_template_code,
                transport,
                selected,
                payload,
                updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setLong(1, MCU_CONSOLE_CONTEXT_ID)
            statement.setString(2, MCU_CONSOLE_CONTEXT_CODE)
            statement.setString(3, MCU_CONSOLE_CONTEXT_NAME)
            statement.setInt(4, 1)
            statement.setString(5, "MCU_CONSOLE")
            statement.setString(6, MCU_CONSOLE_PROTOCOL_TEMPLATE_CODE)
            statement.setString(7, ModbusTransportKind.RTU.transportId)
            statement.setInt(8, 1)
            statement.setString(9, ModbusMetadataJsonCodec.encodeServices(buildDefaultMcuConsoleSnapshotServices()))
            statement.setLong(10, SNAPSHOT_UPDATED_AT)
            statement.executeUpdate()
        }
    }
}

internal fun buildDefaultMcuConsoleSnapshotServices(
    transport: ModbusTransportKind = ModbusTransportKind.RTU,
): List<ModbusServiceModel> {
    val readOperations =
        ModbusContractDefaultsResolver.resolveOperationIdsAndQuantities(
            listOf(
                buildReadOperation(
                    transport = transport,
                    methodName = "get24PowerLights",
                    summary = "读取 24 路电源灯状态。",
                    address = 0,
                    functionCodeName = "READ_COILS",
                    returnSimpleName = "Device24PowerLightsRegisters",
                    properties =
                        (1..24).map { index ->
                            booleanProperty(
                                name = "light$index",
                                registerOffset = index - 1,
                                doc = "电源灯 $index",
                            )
                        },
                ),
                buildReadOperation(
                    transport = transport,
                    methodName = "getDeviceInfo",
                    summary = "读取板子的固件版本、CPU、晶振、Flash 容量和 MAC 地址。",
                    address = 100,
                    functionCodeName = "READ_INPUT_REGISTERS",
                    returnSimpleName = "DeviceRuntimeInfoRegisters",
                    properties =
                        listOf(
                            stringProperty("firmwareVersion", registerOffset = 0, length = 8, doc = "固件版本"),
                            stringProperty("cpuModel", registerOffset = 8, length = 8, doc = "CPU 型号"),
                            intProperty("xtalFrequencyHz", codecName = "U32_BE", registerOffset = 16, doc = "晶振频率"),
                            intProperty("flashSizeBytes", codecName = "U32_BE", registerOffset = 18, doc = "Flash 容量"),
                            stringProperty("macAddress", registerOffset = 20, length = 9, doc = "MAC 地址"),
                        ),
                ),
                buildReadOperation(
                    transport = transport,
                    methodName = "getFlashConfig",
                    summary = "读取 Flash 持久化配置。",
                    address = 200,
                    functionCodeName = "READ_HOLDING_REGISTERS",
                    returnSimpleName = "FlashConfigRegisters",
                    properties = flashConfigProperties(),
                ),
            ),
        )
    val writeOperations =
        ModbusContractDefaultsResolver.resolveOperationIdsAndQuantities(
            listOf(
                buildWriteOperation(
                    transport = transport,
                    methodName = "writeIndicatorLights",
                    summary = "设置故障灯和运行灯。",
                    address = 24,
                    functionCodeName = "WRITE_MULTIPLE_COILS",
                    parameters =
                        listOf(
                            booleanParameter("faultLightOn", order = 0, registerOffset = 0, doc = "故障灯开关状态。"),
                            booleanParameter("runLightOn", order = 1, registerOffset = 1, doc = "运行灯开关状态。"),
                        ),
                ),
                buildWriteOperation(
                    transport = transport,
                    methodName = "writeFlashConfig",
                    summary = "写入 Flash 持久化配置。",
                    address = 200,
                    functionCodeName = "WRITE_MULTIPLE_REGISTERS",
                    parameters = flashConfigParameters(),
                ),
            ),
        )
    return listOf(
        ModbusServiceModel(
            interfacePackage = MCU_DEVICE_PACKAGE,
            interfaceSimpleName = "DeviceApi",
            interfaceQualifiedName = "$MCU_DEVICE_PACKAGE.DeviceApi",
            serviceId = ModbusContractDefaultsResolver.defaultServiceId("DeviceApi"),
            summary = MCU_CONSOLE_CONTEXT_NAME,
            basePath = "/api/modbus",
            transport = transport,
            doc = ModbusDocModel(summary = MCU_CONSOLE_CONTEXT_NAME),
            operations = readOperations,
        ),
        ModbusServiceModel(
            interfacePackage = MCU_DEVICE_PACKAGE,
            interfaceSimpleName = "DeviceWriteApi",
            interfaceQualifiedName = "$MCU_DEVICE_PACKAGE.DeviceWriteApi",
            serviceId = ModbusContractDefaultsResolver.defaultServiceId("DeviceWriteApi"),
            summary = MCU_CONSOLE_CONTEXT_NAME,
            basePath = "/api/modbus",
            transport = transport,
            doc = ModbusDocModel(summary = MCU_CONSOLE_CONTEXT_NAME),
            operations = writeOperations,
        ),
    )
}

private fun buildReadOperation(
    transport: ModbusTransportKind,
    methodName: String,
    summary: String,
    address: Int,
    functionCodeName: String,
    returnSimpleName: String,
    properties: List<ModbusPropertyModel>,
): ModbusOperationModel =
    ModbusOperationModel(
        methodName = methodName,
        operationId = "",
        functionCodeName = functionCodeName,
        address = address,
        quantity = -1,
        requestClassName = buildRequestClassName("DeviceApi", transport, methodName),
        requestQualifiedName = buildRequestQualifiedName("DeviceApi", transport, methodName),
        parameters = emptyList(),
        returnType =
            ModbusReturnTypeModel(
                qualifiedName = "$MCU_DEVICE_PACKAGE.$returnSimpleName",
                simpleName = returnSimpleName,
                kind = ModbusReturnKind.DTO,
                docSummary = summary,
                properties = properties,
            ),
        doc = ModbusDocModel(summary = summary),
    )

private fun buildWriteOperation(
    transport: ModbusTransportKind,
    methodName: String,
    summary: String,
    address: Int,
    functionCodeName: String,
    parameters: List<ModbusParameterModel>,
): ModbusOperationModel =
    ModbusOperationModel(
        methodName = methodName,
        operationId = "",
        functionCodeName = functionCodeName,
        address = address,
        quantity = -1,
        requestClassName = buildRequestClassName("DeviceWriteApi", transport, methodName),
        requestQualifiedName = buildRequestQualifiedName("DeviceWriteApi", transport, methodName),
        parameters = parameters,
        returnType =
            ModbusReturnTypeModel(
                qualifiedName = "site.addzero.device.protocol.modbus.model.ModbusCommandResult",
                simpleName = "ModbusCommandResult",
                kind = ModbusReturnKind.COMMAND_RESULT,
            ),
        doc = ModbusDocModel(summary = summary),
    )

private fun flashConfigProperties(): List<ModbusPropertyModel> =
    listOf(
        intProperty("magicWord", codecName = "U32_BE", registerOffset = 0, doc = "魔术字：0x5A5A5A5A，校验 Flash 数据是否已初始化。"),
        bytesProperty("portConfig", registerOffset = 2, length = 24, doc = "24 路端口配置。"),
        bytesProperty("uartParams", registerOffset = 14, length = 16, doc = "串口参数（波特率、校验位等）。"),
        intProperty("slaveAddress", codecName = "U8", registerOffset = 22, doc = "Modbus 从机地址。"),
        bytesProperty("debounceParams", registerOffset = 23, length = 4, doc = "抖动采样参数（阈值，范围 1-255，推荐 5）。"),
        intProperty("modbusInterval", codecName = "U16", registerOffset = 25, doc = "Modbus 帧时间间隔，单位 ms。"),
        intProperty("wdtEnable", codecName = "U8", registerOffset = 26, doc = "看门狗硬件使能，0 表示关闭，1 表示开启。"),
        intProperty("firmwareUpgrade", codecName = "U8", registerOffset = 27, doc = "固件升级标志，0 表示不升级，1 表示升级。"),
        bytesProperty("diHardwareFirmware", registerOffset = 28, length = 2, doc = "DI 模块硬件固件版本号，低 8 位为次版本号，高 8 位为主版本号。"),
        bytesProperty("diStatus", registerOffset = 29, length = 3, doc = "24 路 DI 状态，每个 bit 代表 1 路，bit[0] = CH1。"),
        intProperty("faultStatus", codecName = "U8", registerOffset = 31, doc = "故障状态标志，位掩码。"),
        intProperty("crc", codecName = "U16", registerOffset = 32, doc = "CRC16 校验，从 magicWord 到 diStatus 字段。"),
    )

private fun flashConfigParameters(): List<ModbusParameterModel> =
    listOf(
        intParameter("magicWord", order = 0, codecName = "U32_BE", registerOffset = 0, doc = "魔术字：0x5A5A5A5A，校验 Flash 数据是否已初始化。"),
        bytesParameter("portConfig", order = 1, registerOffset = 2, length = 24, doc = "24 路端口配置。"),
        bytesParameter("uartParams", order = 2, registerOffset = 14, length = 16, doc = "串口参数（波特率、校验位等）。"),
        intParameter("slaveAddress", order = 3, codecName = "U8", registerOffset = 22, doc = "Modbus 从机地址。"),
        bytesParameter("debounceParams", order = 4, registerOffset = 23, length = 4, doc = "抖动采样参数（阈值，范围 1-255，推荐 5）。"),
        intParameter("modbusInterval", order = 5, codecName = "U16", registerOffset = 25, doc = "Modbus 帧时间间隔，单位 ms。"),
        intParameter("wdtEnable", order = 6, codecName = "U8", registerOffset = 26, doc = "看门狗硬件使能，0 表示关闭，1 表示开启。"),
        intParameter("firmwareUpgrade", order = 7, codecName = "U8", registerOffset = 27, doc = "固件升级标志，0 表示不升级，1 表示升级。"),
        bytesParameter("diHardwareFirmware", order = 8, registerOffset = 28, length = 2, doc = "DI 模块硬件固件版本号，低 8 位为次版本号，高 8 位为主版本号。"),
        bytesParameter("diStatus", order = 9, registerOffset = 29, length = 3, doc = "24 路 DI 状态，每个 bit 代表 1 路，bit[0] = CH1。"),
        intParameter("faultStatus", order = 10, codecName = "U8", registerOffset = 31, doc = "故障状态标志，位掩码。"),
        intParameter("crc", order = 11, codecName = "U16", registerOffset = 32, doc = "CRC16 校验，从 magicWord 到 diStatus 字段。"),
    )

private fun booleanProperty(
    name: String,
    registerOffset: Int,
    doc: String,
): ModbusPropertyModel =
    ModbusPropertyModel(
        name = name,
        qualifiedType = "kotlin.Boolean",
        valueKind = ModbusValueKind.BOOLEAN,
        field =
            ModbusFieldModel(
                codecName = "BOOL_COIL",
                registerOffset = registerOffset,
                bitOffset = 0,
                length = 1,
                registerWidth = 1,
            ),
        doc = doc,
    )

private fun stringProperty(
    name: String,
    registerOffset: Int,
    length: Int,
    doc: String,
): ModbusPropertyModel =
    ModbusPropertyModel(
        name = name,
        qualifiedType = "kotlin.String",
        valueKind = ModbusValueKind.STRING,
        field =
            ModbusFieldModel(
                codecName = "STRING_ASCII",
                registerOffset = registerOffset,
                bitOffset = 0,
                length = length,
                registerWidth = registerWidth("STRING_ASCII", length),
            ),
        doc = doc,
    )

private fun intProperty(
    name: String,
    codecName: String,
    registerOffset: Int,
    doc: String,
): ModbusPropertyModel =
    ModbusPropertyModel(
        name = name,
        qualifiedType = "kotlin.Int",
        valueKind = ModbusValueKind.INT,
        field =
            ModbusFieldModel(
                codecName = codecName,
                registerOffset = registerOffset,
                bitOffset = 0,
                length = 1,
                registerWidth = registerWidth(codecName, 1),
            ),
        doc = doc,
    )

private fun bytesProperty(
    name: String,
    registerOffset: Int,
    length: Int,
    doc: String,
): ModbusPropertyModel =
    ModbusPropertyModel(
        name = name,
        qualifiedType = "kotlin.ByteArray",
        valueKind = ModbusValueKind.BYTES,
        field =
            ModbusFieldModel(
                codecName = "BYTE_ARRAY",
                registerOffset = registerOffset,
                bitOffset = 0,
                length = length,
                registerWidth = registerWidth("BYTE_ARRAY", length),
            ),
        doc = doc,
    )

private fun booleanParameter(
    name: String,
    order: Int,
    registerOffset: Int,
    doc: String,
): ModbusParameterModel =
    ModbusParameterModel(
        name = name,
        qualifiedType = "kotlin.Boolean",
        valueKind = ModbusValueKind.BOOLEAN,
        order = order,
        codecName = "BOOL_COIL",
        registerOffset = registerOffset,
        bitOffset = 0,
        registerWidth = 1,
        length = 1,
        doc = doc,
    )

private fun intParameter(
    name: String,
    order: Int,
    codecName: String,
    registerOffset: Int,
    doc: String,
): ModbusParameterModel =
    ModbusParameterModel(
        name = name,
        qualifiedType = "kotlin.Int",
        valueKind = ModbusValueKind.INT,
        order = order,
        codecName = codecName,
        registerOffset = registerOffset,
        bitOffset = 0,
        registerWidth = registerWidth(codecName, 1),
        length = 1,
        doc = doc,
    )

private fun bytesParameter(
    name: String,
    order: Int,
    registerOffset: Int,
    length: Int,
    doc: String,
): ModbusParameterModel =
    ModbusParameterModel(
        name = name,
        qualifiedType = "kotlin.ByteArray",
        valueKind = ModbusValueKind.BYTES,
        order = order,
        codecName = "BYTE_ARRAY",
        registerOffset = registerOffset,
        bitOffset = 0,
        registerWidth = registerWidth("BYTE_ARRAY", length),
        length = length,
        doc = doc,
    )

private fun buildRequestClassName(
    interfaceSimpleName: String,
    transport: ModbusTransportKind,
    methodName: String,
): String {
    val prefix = interfaceSimpleName + transport.transportId.replaceFirstChar(Char::uppercase)
    return prefix + methodName.replaceFirstChar(Char::uppercase) + "Request"
}

private fun buildRequestQualifiedName(
    interfaceSimpleName: String,
    transport: ModbusTransportKind,
    methodName: String,
): String = "$MCU_DEVICE_PACKAGE.generated.${buildRequestClassName(interfaceSimpleName, transport, methodName)}"
