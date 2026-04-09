package site.addzero.kcloud.plugins.hostconfig.api.template

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Serializable
/**
 * 表示协议模板元数据。
 *
 * @property transportType 传输类型。
 * @property transportForm 协议传输表单元数据。
 */
data class ProtocolTemplateMetadataResponse(
    val transportType: TransportType? = null,
    val transportForm: ProtocolTransportFormMetadataResponse? = null,
)

@Serializable
/**
 * 表示协议传输表单元数据。
 *
 * @property title 分组标题。
 * @property subtitle 分组副标题。
 * @property fields 字段列表。
 * @property summaryKeys 摘要展示字段。
 */
data class ProtocolTransportFormMetadataResponse(
    val title: String = "通信配置",
    val subtitle: String? = null,
    val fields: List<ProtocolTransportFieldMetadataResponse> = emptyList(),
    val summaryKeys: List<ProtocolTransportFieldKey> = emptyList(),
)

@Serializable
/**
 * 表示协议传输字段元数据。
 *
 * @property key 字段键。
 * @property label 字段标题。
 * @property widget 控件类型。
 * @property required 是否必填。
 * @property placeholder 占位提示。
 * @property helperText 辅助提示。
 * @property defaultValue 默认值。
 * @property options 选项列表。
 */
data class ProtocolTransportFieldMetadataResponse(
    val key: ProtocolTransportFieldKey,
    val label: String,
    val widget: ProtocolTransportFieldWidget = ProtocolTransportFieldWidget.TEXT,
    val required: Boolean = false,
    val placeholder: String? = null,
    val helperText: String? = null,
    val defaultValue: String? = null,
    val options: List<ProtocolTransportFieldOptionResponse> = emptyList(),
)

@Serializable
/**
 * 表示协议传输字段选项。
 *
 * @property value 实际值。
 * @property label 展示文案。
 * @property description 说明。
 */
data class ProtocolTransportFieldOptionResponse(
    val value: String,
    val label: String,
    val description: String? = null,
)

@Serializable
/**
 * 定义协议传输字段键。
 */
enum class ProtocolTransportFieldKey {
    HOST,
    TCP_PORT,
    PORT_NAME,
    BAUD_RATE,
    DATA_BITS,
    STOP_BITS,
    PARITY,
    RESPONSE_TIMEOUT_MS,
}

@Serializable
/**
 * 定义协议传输字段控件类型。
 */
enum class ProtocolTransportFieldWidget {
    TEXT,
    NUMBER,
    SELECT,
}

/**
 * 根据协议模板元数据构造默认传输配置。
 */
fun ProtocolTemplateMetadataResponse.defaultTransportConfig(): ProtocolTransportConfig? {
    return buildTransportConfig { key ->
        transportForm
            ?.fields
            ?.firstOrNull { field -> field.key == key }
            ?.defaultValue
    }
}

/**
 * 根据协议模板元数据和字段取值构造传输配置。
 *
 * @param valueProvider 字段取值提供器。
 */
fun ProtocolTemplateMetadataResponse.buildTransportConfig(
    valueProvider: (ProtocolTransportFieldKey) -> String?,
): ProtocolTransportConfig? {
    val resolvedTransportType = transportType ?: return null
    var host: String? = null
    var tcpPort: Int? = null
    var portName: String? = null
    var baudRate: Int? = null
    var dataBits: Int? = null
    var stopBits: Int? = null
    var parity: Parity? = null
    var responseTimeoutMs: Int? = null

    transportForm?.fields?.forEach { field ->
        val rawValue = valueProvider(field.key).orEmpty().ifBlank { field.defaultValue.orEmpty() }
        when (field.key) {
            ProtocolTransportFieldKey.HOST -> {
                host = rawValue.ifBlank { null }
            }

            ProtocolTransportFieldKey.TCP_PORT -> {
                tcpPort = rawValue.toIntOrNull()
            }

            ProtocolTransportFieldKey.PORT_NAME -> {
                portName = rawValue.ifBlank { null }
            }

            ProtocolTransportFieldKey.BAUD_RATE -> {
                baudRate = rawValue.toIntOrNull()
            }

            ProtocolTransportFieldKey.DATA_BITS -> {
                dataBits = rawValue.toIntOrNull()
            }

            ProtocolTransportFieldKey.STOP_BITS -> {
                stopBits = rawValue.toIntOrNull()
            }

            ProtocolTransportFieldKey.PARITY -> {
                parity = Parity.entries.firstOrNull { option -> option.name == rawValue }
            }

            ProtocolTransportFieldKey.RESPONSE_TIMEOUT_MS -> {
                responseTimeoutMs = rawValue.toIntOrNull()
            }
        }
    }

    return ProtocolTransportConfig(
        transportType = resolvedTransportType,
        host = host,
        tcpPort = tcpPort,
        portName = portName,
        baudRate = baudRate,
        dataBits = dataBits,
        stopBits = stopBits,
        parity = parity,
        responseTimeoutMs = responseTimeoutMs,
    )
}

/**
 * 读取传输配置字段值。
 *
 * @param key 字段键。
 */
fun ProtocolTransportConfig.fieldValue(
    key: ProtocolTransportFieldKey,
): String {
    return when (key) {
        ProtocolTransportFieldKey.HOST -> host.orEmpty()
        ProtocolTransportFieldKey.TCP_PORT -> tcpPort?.toString().orEmpty()
        ProtocolTransportFieldKey.PORT_NAME -> portName.orEmpty()
        ProtocolTransportFieldKey.BAUD_RATE -> baudRate?.toString().orEmpty()
        ProtocolTransportFieldKey.DATA_BITS -> dataBits?.toString().orEmpty()
        ProtocolTransportFieldKey.STOP_BITS -> stopBits?.toString().orEmpty()
        ProtocolTransportFieldKey.PARITY -> parity?.name.orEmpty()
        ProtocolTransportFieldKey.RESPONSE_TIMEOUT_MS -> responseTimeoutMs?.toString().orEmpty()
    }
}
