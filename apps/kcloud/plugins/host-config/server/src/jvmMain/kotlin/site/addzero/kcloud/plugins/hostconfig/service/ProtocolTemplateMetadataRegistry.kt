package site.addzero.kcloud.plugins.hostconfig.service

import kotlinx.serialization.decodeFromString
import site.addzero.core.network.json.json
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTemplateMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldKey
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldWidget
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFormMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

/**
 * 集中定义协议模板元数据。
 */
internal object ProtocolTemplateMetadataRegistry {

    /**
     * 解析协议模板元数据。
     *
     * @param templateCode 模板编码。
     * @param metadataJson 元数据 JSON。
     */
    fun resolve(
        templateCode: String,
        metadataJson: String?,
    ): ProtocolTemplateMetadataResponse? {
        val rawJson = metadataJson?.takeIf { it.isNotBlank() }
        if (rawJson != null) {
            return runCatching {
                json.decodeFromString<ProtocolTemplateMetadataResponse>(rawJson)
            }.getOrElse {
                defaultFor(templateCode)
            }
        }
        return defaultFor(templateCode)
    }

    /**
     * 返回协议模板默认元数据。
     *
     * @param templateCode 模板编码。
     */
    fun defaultFor(
        templateCode: String,
    ): ProtocolTemplateMetadataResponse? {
        return when (templateCode) {
            "MODBUS_RTU_CLIENT" -> modbusRtuMetadata()
            "MODBUS_TCP_CLIENT" -> modbusTcpMetadata()
            "MQTT_CLIENT" -> mqttMetadata()
            else -> null
        }
    }

    /**
     * 构建 RTU 元数据。
     */
    private fun modbusRtuMetadata(): ProtocolTemplateMetadataResponse {
        return ProtocolTemplateMetadataResponse(
            transportType = TransportType.RTU,
            transportForm = ProtocolTransportFormMetadataResponse(
                title = "通信配置",
                subtitle = "协议模板元数据决定字段集合，新增 RTU 类协议时不再改界面分支。",
                summaryKeys = listOf(
                    ProtocolTransportFieldKey.PORT_NAME,
                    ProtocolTransportFieldKey.BAUD_RATE,
                    ProtocolTransportFieldKey.PARITY,
                ),
                fields = listOf(
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.PORT_NAME,
                        label = "串口",
                        required = true,
                        placeholder = "例如 COM4",
                    ),
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.BAUD_RATE,
                        label = "波特率",
                        widget = ProtocolTransportFieldWidget.SELECT,
                        required = true,
                        defaultValue = "9600",
                        helperText = "300-115200bps 可选",
                        options = baudRateOptions(),
                    ),
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.DATA_BITS,
                        label = "数据位",
                        widget = ProtocolTransportFieldWidget.SELECT,
                        required = true,
                        defaultValue = "8",
                        helperText = "7 位、8 位可选",
                        options = listOf("7", "8").map { value ->
                            ProtocolTransportFieldOptionResponse(
                                value = value,
                                label = value,
                            )
                        },
                    ),
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.STOP_BITS,
                        label = "停止位",
                        widget = ProtocolTransportFieldWidget.SELECT,
                        required = true,
                        defaultValue = "1",
                        helperText = "1 位、2 位可选",
                        options = listOf("1", "2").map { value ->
                            ProtocolTransportFieldOptionResponse(
                                value = value,
                                label = value,
                            )
                        },
                    ),
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.PARITY,
                        label = "校验位",
                        widget = ProtocolTransportFieldWidget.SELECT,
                        required = true,
                        defaultValue = Parity.NONE.name,
                        options = Parity.entries.map { option ->
                            ProtocolTransportFieldOptionResponse(
                                value = option.name,
                                label = option.name,
                            )
                        },
                    ),
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.RESPONSE_TIMEOUT_MS,
                        label = "响应超时(ms)",
                        widget = ProtocolTransportFieldWidget.NUMBER,
                        defaultValue = "1000",
                        helperText = "按协议模板元数据控制默认值和提示文案。",
                    ),
                ),
            ),
        )
    }

    /**
     * 构建 TCP 元数据。
     */
    private fun modbusTcpMetadata(): ProtocolTemplateMetadataResponse {
        return ProtocolTemplateMetadataResponse(
            transportType = TransportType.TCP,
            transportForm = ProtocolTransportFormMetadataResponse(
                title = "通信配置",
                subtitle = "协议模板元数据决定字段集合，新增 TCP 类协议时只补模板元数据。",
                summaryKeys = listOf(
                    ProtocolTransportFieldKey.HOST,
                    ProtocolTransportFieldKey.TCP_PORT,
                ),
                fields = listOf(
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.HOST,
                        label = "主机地址",
                        required = true,
                        placeholder = "例如 192.168.1.10",
                    ),
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.TCP_PORT,
                        label = "TCP 端口",
                        widget = ProtocolTransportFieldWidget.NUMBER,
                        required = true,
                        defaultValue = "502",
                        placeholder = "默认 502",
                    ),
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.RESPONSE_TIMEOUT_MS,
                        label = "响应超时(ms)",
                        widget = ProtocolTransportFieldWidget.NUMBER,
                        defaultValue = "1000",
                    ),
                ),
            ),
        )
    }

    /**
     * 构建 MQTT 元数据。
     */
    private fun mqttMetadata(): ProtocolTemplateMetadataResponse {
        return ProtocolTemplateMetadataResponse(
            transportType = null,
            transportForm = ProtocolTransportFormMetadataResponse(
                title = "通信配置",
                subtitle = "当前 MQTT 模板暂未定义额外通信字段。",
                fields = emptyList(),
                summaryKeys = emptyList(),
            ),
        )
    }

    /**
     * 构建波特率选项。
     */
    private fun baudRateOptions(): List<ProtocolTransportFieldOptionResponse> {
        return listOf(
            300,
            600,
            1200,
            2400,
            4800,
            9600,
            19200,
            38400,
            57600,
            115200,
        ).map { value ->
            ProtocolTransportFieldOptionResponse(
                value = value.toString(),
                label = value.toString(),
            )
        }
    }
}
