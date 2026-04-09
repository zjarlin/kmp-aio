package site.addzero.esp32_host_computer.generated.modbus.rtu

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.mp.KoinPlatform
import site.addzero.device.driver.modbus.rtu.DefaultModbusRtuEndpointConfig
import site.addzero.device.driver.modbus.rtu.ModbusRtuEndpointConfig
import site.addzero.device.driver.modbus.rtu.ModbusRtuExecutor
import site.addzero.device.driver.modbus.rtu.ModbusSerialParity
import site.addzero.device.protocol.modbus.ModbusCodecSupport
import site.addzero.device.protocol.modbus.model.ModbusCodec

internal interface GeneratedModbusRtuRequestConfig {
    val portPath: String?
    val unitId: Int?
    val baudRate: Int?
    val dataBits: Int?
    val stopBits: Int?
    val parity: ModbusSerialParity?
    val timeoutMs: Long?
    val retries: Int?
}

internal fun GeneratedModbusRtuRequestConfig.toEndpointConfig(defaultConfig: ModbusRtuEndpointConfig): ModbusRtuEndpointConfig =
    DefaultModbusRtuEndpointConfig(
        portPath = portPath ?: defaultConfig.portPath,
        unitId = unitId ?: defaultConfig.unitId,
        baudRate = baudRate ?: defaultConfig.baudRate,
        dataBits = dataBits ?: defaultConfig.dataBits,
        stopBits = stopBits ?: defaultConfig.stopBits,
        parity = parity ?: defaultConfig.parity,
        timeoutMs = timeoutMs ?: defaultConfig.timeoutMs,
        retries = retries ?: defaultConfig.retries,
    )
/**
 * 读取 24 路电源灯状态。
 */
@Serializable
data class DeviceApiRtuGet24PowerLightsRequest(
override val portPath: String? = null,
override val unitId: Int? = null,
override val baudRate: Int? = null,
override val dataBits: Int? = null,
override val stopBits: Int? = null,
override val parity: ModbusSerialParity? = null,
override val timeoutMs: Long? = null,
override val retries: Int? = null,
) : GeneratedModbusRtuRequestConfig

/**
 * 读取板子的固件版本、CPU、晶振、Flash 容量和 MAC 地址。
 */
@Serializable
data class DeviceApiRtuGetDeviceInfoRequest(
override val portPath: String? = null,
override val unitId: Int? = null,
override val baudRate: Int? = null,
override val dataBits: Int? = null,
override val stopBits: Int? = null,
override val parity: ModbusSerialParity? = null,
override val timeoutMs: Long? = null,
override val retries: Int? = null,
) : GeneratedModbusRtuRequestConfig

/**
 * 读取 Flash 持久化配置。
 */
@Serializable
data class DeviceApiRtuGetFlashConfigRequest(
override val portPath: String? = null,
override val unitId: Int? = null,
override val baudRate: Int? = null,
override val dataBits: Int? = null,
override val stopBits: Int? = null,
override val parity: ModbusSerialParity? = null,
override val timeoutMs: Long? = null,
override val retries: Int? = null,
) : GeneratedModbusRtuRequestConfig


/**
 * 接管 mcu-console 当前默认的设备读写契约。
 *
 * 该 gateway 由 KSP 自动生成，负责把高阶 Kotlin 接口翻译成 Modbus RTU 调用。
 */
class DeviceApiGeneratedRtuGateway(private val configuredDefaultConfig: ModbusRtuEndpointConfig, private val executor: ModbusRtuExecutor) : site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceApi {
    fun defaultConfig(): ModbusRtuEndpointConfig = configuredDefaultConfig

    private fun resolveConfig(config: ModbusRtuEndpointConfig?): ModbusRtuEndpointConfig =
        config ?: defaultConfig()

    override suspend fun get24PowerLights(): site.addzero.kcloud.plugins.mcuconsole.modbus.device.Device24PowerLightsRegisters = get24PowerLights(config = null)

    suspend fun get24PowerLights(config: ModbusRtuEndpointConfig? = null): site.addzero.kcloud.plugins.mcuconsole.modbus.device.Device24PowerLightsRegisters {
        val resolvedConfig = resolveConfig(config)
        val registers = executor.readCoils(resolvedConfig, 0, 24)
        return site.addzero.kcloud.plugins.mcuconsole.modbus.device.Device24PowerLightsRegisters(
            light1 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 0, 0),
            light2 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 1, 0),
            light3 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 2, 0),
            light4 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 3, 0),
            light5 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 4, 0),
            light6 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 5, 0),
            light7 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 6, 0),
            light8 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 7, 0),
            light9 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 8, 0),
            light10 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 9, 0),
            light11 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 10, 0),
            light12 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 11, 0),
            light13 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 12, 0),
            light14 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 13, 0),
            light15 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 14, 0),
            light16 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 15, 0),
            light17 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 16, 0),
            light18 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 17, 0),
            light19 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 18, 0),
            light20 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 19, 0),
            light21 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 20, 0),
            light22 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 21, 0),
            light23 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 22, 0),
            light24 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 23, 0)
        )
    }

    override suspend fun getDeviceInfo(): site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceRuntimeInfoRegisters = getDeviceInfo(config = null)

    suspend fun getDeviceInfo(config: ModbusRtuEndpointConfig? = null): site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceRuntimeInfoRegisters {
        val resolvedConfig = resolveConfig(config)
        val registers = executor.readInputRegisters(resolvedConfig, 100, 29)
        return site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceRuntimeInfoRegisters(
            firmwareVersion = ModbusCodecSupport.decodeString(ModbusCodec.STRING_ASCII, registers, 0, 8),
            cpuModel = ModbusCodecSupport.decodeString(ModbusCodec.STRING_ASCII, registers, 8, 8),
            xtalFrequencyHz = ModbusCodecSupport.decodeInt(ModbusCodec.U32_BE, registers, 16),
            flashSizeBytes = ModbusCodecSupport.decodeInt(ModbusCodec.U32_BE, registers, 18),
            macAddress = ModbusCodecSupport.decodeString(ModbusCodec.STRING_ASCII, registers, 20, 9)
        )
    }

    override suspend fun getFlashConfig(): site.addzero.kcloud.plugins.mcuconsole.modbus.device.FlashConfigRegisters = getFlashConfig(config = null)

    suspend fun getFlashConfig(config: ModbusRtuEndpointConfig? = null): site.addzero.kcloud.plugins.mcuconsole.modbus.device.FlashConfigRegisters {
        val resolvedConfig = resolveConfig(config)
        val registers = executor.readHoldingRegisters(resolvedConfig, 200, 33)
        return site.addzero.kcloud.plugins.mcuconsole.modbus.device.FlashConfigRegisters(
            magicWord = ModbusCodecSupport.decodeInt(ModbusCodec.U32_BE, registers, 0),
            portConfig = ModbusCodecSupport.decodeByteArray(ModbusCodec.BYTE_ARRAY, registers, 2, 24),
            uartParams = ModbusCodecSupport.decodeByteArray(ModbusCodec.BYTE_ARRAY, registers, 14, 16),
            slaveAddress = ModbusCodecSupport.decodeInt(ModbusCodec.U8, registers, 22),
            debounceParams = ModbusCodecSupport.decodeByteArray(ModbusCodec.BYTE_ARRAY, registers, 23, 4),
            modbusInterval = ModbusCodecSupport.decodeInt(ModbusCodec.U16, registers, 25),
            wdtEnable = ModbusCodecSupport.decodeInt(ModbusCodec.U8, registers, 26),
            firmwareUpgrade = ModbusCodecSupport.decodeInt(ModbusCodec.U8, registers, 27),
            diHardwareFirmware = ModbusCodecSupport.decodeByteArray(ModbusCodec.BYTE_ARRAY, registers, 28, 2),
            diStatus = ModbusCodecSupport.decodeByteArray(ModbusCodec.BYTE_ARRAY, registers, 29, 3),
            faultStatus = ModbusCodecSupport.decodeInt(ModbusCodec.U8, registers, 31),
            crc = ModbusCodecSupport.decodeInt(ModbusCodec.U16, registers, 32)
        )
    }

}

/**
 * 设置故障灯和运行灯。
 */
@Serializable
data class DeviceWriteApiRtuWriteIndicatorLightsRequest(
override val portPath: String? = null,
override val unitId: Int? = null,
override val baudRate: Int? = null,
override val dataBits: Int? = null,
override val stopBits: Int? = null,
override val parity: ModbusSerialParity? = null,
override val timeoutMs: Long? = null,
override val retries: Int? = null,
    /** 故障灯 */
    val faultLightOn: Boolean,
    /** 运行灯 */
    val runLightOn: Boolean
) : GeneratedModbusRtuRequestConfig

/**
 * 写入 Flash 持久化配置。
 */
@Serializable
data class DeviceWriteApiRtuWriteFlashConfigRequest(
override val portPath: String? = null,
override val unitId: Int? = null,
override val baudRate: Int? = null,
override val dataBits: Int? = null,
override val stopBits: Int? = null,
override val parity: ModbusSerialParity? = null,
override val timeoutMs: Long? = null,
override val retries: Int? = null,
    /** 魔术字：0x5A5A5A5A，校验 Flash 数据是否已初始化。 */
    val magicWord: Int,
    /** 24 路端口配置。 */
    val portConfig: ByteArray,
    /** 串口参数（波特率、校验位等）。 */
    val uartParams: ByteArray,
    /** Modbus 从机地址。 */
    val slaveAddress: Int,
    /** 抖动采样参数（阈值，范围 1-255，推荐 5）。 */
    val debounceParams: ByteArray,
    /** Modbus 帧时间间隔，单位 ms。 */
    val modbusInterval: Int,
    /** 看门狗硬件使能，0 表示关闭，1 表示开启。 */
    val wdtEnable: Int,
    /** 固件升级标志，0 表示不升级，1 表示升级。 */
    val firmwareUpgrade: Int,
    /** DI 模块硬件固件版本号，低 8 位为次版本号，高 8 位为主版本号。 */
    val diHardwareFirmware: ByteArray,
    /** 24 路 DI 状态，每个 bit 代表 1 路，bit[0] = CH1。 */
    val diStatus: ByteArray,
    /** 故障状态标志，位掩码。 */
    val faultStatus: Int,
    /** CRC16 校验，从 magicWord 到 diStatus 字段。 */
    val crc: Int
) : GeneratedModbusRtuRequestConfig


/**
 * 接管 mcu-console 当前默认的设备读写契约。
 *
 * 该 gateway 由 KSP 自动生成，负责把高阶 Kotlin 接口翻译成 Modbus RTU 调用。
 */
class DeviceWriteApiGeneratedRtuGateway(private val configuredDefaultConfig: ModbusRtuEndpointConfig, private val executor: ModbusRtuExecutor) : site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceWriteApi {
    fun defaultConfig(): ModbusRtuEndpointConfig = configuredDefaultConfig

    private fun resolveConfig(config: ModbusRtuEndpointConfig?): ModbusRtuEndpointConfig =
        config ?: defaultConfig()

    override suspend fun writeIndicatorLights(faultLightOn: Boolean, runLightOn: Boolean): site.addzero.device.protocol.modbus.model.ModbusCommandResult = writeIndicatorLights(config = null, faultLightOn = faultLightOn, runLightOn = runLightOn)

    suspend fun writeIndicatorLights(config: ModbusRtuEndpointConfig? = null, faultLightOn: Boolean, runLightOn: Boolean): site.addzero.device.protocol.modbus.model.ModbusCommandResult {
        val resolvedConfig = resolveConfig(config)
        try {
            val coilValues = MutableList(2) { false }
            coilValues[0] = faultLightOn
            coilValues[1] = runLightOn
            executor.writeMultipleCoils(resolvedConfig, 24, coilValues)
            return site.addzero.device.protocol.modbus.model.ModbusCommandResult(accepted = true, summary = "操作已下发：write-indicator-lights")
        } catch (exception: site.addzero.modbus.ModbusProtocolException) {
            return site.addzero.device.protocol.modbus.model.ModbusCommandResult(
                accepted = false,
                summary = exception.message ?: "操作失败：write-indicator-lights",
                functionCode = exception.functionCode,
                exceptionCode = exception.exceptionCode,
                exceptionName = exception.exceptionName,
            )
        }
    }

    override suspend fun writeFlashConfig(magicWord: Int, portConfig: ByteArray, uartParams: ByteArray, slaveAddress: Int, debounceParams: ByteArray, modbusInterval: Int, wdtEnable: Int, firmwareUpgrade: Int, diHardwareFirmware: ByteArray, diStatus: ByteArray, faultStatus: Int, crc: Int): site.addzero.device.protocol.modbus.model.ModbusCommandResult = writeFlashConfig(config = null, magicWord = magicWord, portConfig = portConfig, uartParams = uartParams, slaveAddress = slaveAddress, debounceParams = debounceParams, modbusInterval = modbusInterval, wdtEnable = wdtEnable, firmwareUpgrade = firmwareUpgrade, diHardwareFirmware = diHardwareFirmware, diStatus = diStatus, faultStatus = faultStatus, crc = crc)

    suspend fun writeFlashConfig(config: ModbusRtuEndpointConfig? = null, magicWord: Int, portConfig: ByteArray, uartParams: ByteArray, slaveAddress: Int, debounceParams: ByteArray, modbusInterval: Int, wdtEnable: Int, firmwareUpgrade: Int, diHardwareFirmware: ByteArray, diStatus: ByteArray, faultStatus: Int, crc: Int): site.addzero.device.protocol.modbus.model.ModbusCommandResult {
        val resolvedConfig = resolveConfig(config)
        try {
            val encodedValues = MutableList(33) { 0 }
            ModbusCodecSupport.encodeValue(ModbusCodec.U32_BE, magicWord.toString())
                .forEachIndexed { index, value -> encodedValues[0 + index] = value }
            ModbusCodecSupport.encodeByteArray(ModbusCodec.BYTE_ARRAY, portConfig, 24)
                .forEachIndexed { index, value -> encodedValues[2 + index] = value }
            ModbusCodecSupport.encodeByteArray(ModbusCodec.BYTE_ARRAY, uartParams, 16)
                .forEachIndexed { index, value -> encodedValues[14 + index] = value }
            ModbusCodecSupport.encodeValue(ModbusCodec.U8, slaveAddress.toString())
                .forEachIndexed { index, value -> encodedValues[22 + index] = value }
            ModbusCodecSupport.encodeByteArray(ModbusCodec.BYTE_ARRAY, debounceParams, 4)
                .forEachIndexed { index, value -> encodedValues[23 + index] = value }
            ModbusCodecSupport.encodeValue(ModbusCodec.U16, modbusInterval.toString())
                .forEachIndexed { index, value -> encodedValues[25 + index] = value }
            ModbusCodecSupport.encodeValue(ModbusCodec.U8, wdtEnable.toString())
                .forEachIndexed { index, value -> encodedValues[26 + index] = value }
            ModbusCodecSupport.encodeValue(ModbusCodec.U8, firmwareUpgrade.toString())
                .forEachIndexed { index, value -> encodedValues[27 + index] = value }
            ModbusCodecSupport.encodeByteArray(ModbusCodec.BYTE_ARRAY, diHardwareFirmware, 2)
                .forEachIndexed { index, value -> encodedValues[28 + index] = value }
            ModbusCodecSupport.encodeByteArray(ModbusCodec.BYTE_ARRAY, diStatus, 3)
                .forEachIndexed { index, value -> encodedValues[29 + index] = value }
            ModbusCodecSupport.encodeValue(ModbusCodec.U8, faultStatus.toString())
                .forEachIndexed { index, value -> encodedValues[31 + index] = value }
            ModbusCodecSupport.encodeValue(ModbusCodec.U16, crc.toString())
                .forEachIndexed { index, value -> encodedValues[32 + index] = value }
            executor.writeMultipleRegisters(resolvedConfig, 200, encodedValues)
            return site.addzero.device.protocol.modbus.model.ModbusCommandResult(accepted = true, summary = "操作已下发：write-flash-config")
        } catch (exception: site.addzero.modbus.ModbusProtocolException) {
            return site.addzero.device.protocol.modbus.model.ModbusCommandResult(
                accepted = false,
                summary = exception.message ?: "操作失败：write-flash-config",
                functionCode = exception.functionCode,
                exceptionCode = exception.exceptionCode,
                exceptionName = exception.exceptionName,
            )
        }
    }

}

/**
 * Modbus RTU 自动生成的 Koin 模块。
 *
 * 统一收口生成出来的网关；默认 RTU 配置由业务自己通过 Koin 提供。
 */
@Module
class GeneratedModbusRtuKoinModule {
    @Single
    fun deviceApiGeneratedRtuGateway(
        defaultConfig: ModbusRtuEndpointConfig,
        executor: ModbusRtuExecutor,
    ): DeviceApiGeneratedRtuGateway = DeviceApiGeneratedRtuGateway(defaultConfig, executor)

    @Single
    fun deviceApi(
        gateway: DeviceApiGeneratedRtuGateway,
    ): site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceApi = gateway

    @Single
    fun deviceWriteApiGeneratedRtuGateway(
        defaultConfig: ModbusRtuEndpointConfig,
        executor: ModbusRtuExecutor,
    ): DeviceWriteApiGeneratedRtuGateway = DeviceWriteApiGeneratedRtuGateway(defaultConfig, executor)

    @Single
    fun deviceWriteApi(
        gateway: DeviceWriteApiGeneratedRtuGateway,
    ): site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceWriteApi = gateway

}

fun Route.registerGeneratedModbusRtuRoutes() {
    post("/api/modbus/rtu/device/get24-power-lights") {
        val request = call.receive<DeviceApiRtuGet24PowerLightsRequest>()
        val gateway = KoinPlatform.getKoin().get<DeviceApiGeneratedRtuGateway>()
        val config = request.toEndpointConfig(gateway.defaultConfig())
        call.respond(gateway.get24PowerLights(config = config))
    }
    post("/api/modbus/rtu/device/get-device-info") {
        val request = call.receive<DeviceApiRtuGetDeviceInfoRequest>()
        val gateway = KoinPlatform.getKoin().get<DeviceApiGeneratedRtuGateway>()
        val config = request.toEndpointConfig(gateway.defaultConfig())
        call.respond(gateway.getDeviceInfo(config = config))
    }
    post("/api/modbus/rtu/device/get-flash-config") {
        val request = call.receive<DeviceApiRtuGetFlashConfigRequest>()
        val gateway = KoinPlatform.getKoin().get<DeviceApiGeneratedRtuGateway>()
        val config = request.toEndpointConfig(gateway.defaultConfig())
        call.respond(gateway.getFlashConfig(config = config))
    }
    post("/api/modbus/rtu/device-write/write-indicator-lights") {
        val request = call.receive<DeviceWriteApiRtuWriteIndicatorLightsRequest>()
        val gateway = KoinPlatform.getKoin().get<DeviceWriteApiGeneratedRtuGateway>()
        val config = request.toEndpointConfig(gateway.defaultConfig())
        call.respond(gateway.writeIndicatorLights(config = config, faultLightOn = request.faultLightOn, runLightOn = request.runLightOn))
    }
    post("/api/modbus/rtu/device-write/write-flash-config") {
        val request = call.receive<DeviceWriteApiRtuWriteFlashConfigRequest>()
        val gateway = KoinPlatform.getKoin().get<DeviceWriteApiGeneratedRtuGateway>()
        val config = request.toEndpointConfig(gateway.defaultConfig())
        call.respond(gateway.writeFlashConfig(config = config, magicWord = request.magicWord, portConfig = request.portConfig, uartParams = request.uartParams, slaveAddress = request.slaveAddress, debounceParams = request.debounceParams, modbusInterval = request.modbusInterval, wdtEnable = request.wdtEnable, firmwareUpgrade = request.firmwareUpgrade, diHardwareFirmware = request.diHardwareFirmware, diStatus = request.diStatus, faultStatus = request.faultStatus, crc = request.crc))
    }
}
