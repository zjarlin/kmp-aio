package site.addzero.kcloud.plugins.hostconfig.api.project

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder2
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder4
import site.addzero.kcloud.plugins.hostconfig.model.enums.FloatOrder
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Serializable
data class ProjectCreateRequest(
    val name: String,
    val description: String? = null,
    val remark: String? = null,
    val sortIndex: Int = 0,
)

@Serializable
data class ProjectUpdateRequest(
    val name: String,
    val description: String? = null,
    val remark: String? = null,
    val sortIndex: Int = 0,
)

@Serializable
data class ProjectResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val remark: String?,
    val sortIndex: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class ProjectPositionUpdateRequest(
    val sortIndex: Int = 0,
)

@Serializable
data class ProjectTreeResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val remark: String?,
    val sortIndex: Int,
    val protocols: List<ProtocolTreeNode>,
    val modules: List<ModuleTreeNode>,
)

@Serializable
data class ProtocolTransportConfig(
    val transportType: TransportType,
    val host: String? = null,
    val tcpPort: Int? = null,
    val portName: String? = null,
    val baudRate: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: Parity? = null,
    val responseTimeoutMs: Int? = null,
)

@Serializable
data class ProtocolTreeNode(
    val id: Long,
    val name: String,
    val pollingIntervalMs: Int,
    val sortIndex: Int,
    val protocolTemplateId: Long,
    val protocolTemplateCode: String,
    val protocolTemplateName: String,
    val transportConfig: ProtocolTransportConfig? = null,
    val modules: List<ModuleTreeNode>,
)

@Serializable
data class ModuleTreeNode(
    val id: Long,
    val name: String,
    val protocolId: Long,
    val sortIndex: Int,
    val moduleTemplateId: Long,
    val moduleTemplateCode: String,
    val moduleTemplateName: String,
    val devices: List<DeviceTreeNode>,
)

@Serializable
data class DeviceTreeNode(
    val id: Long,
    val name: String,
    val stationNo: Int,
    val requestIntervalMs: Int?,
    val writeIntervalMs: Int?,
    val byteOrder2: ByteOrder2?,
    val byteOrder4: ByteOrder4?,
    val floatOrder: FloatOrder?,
    val batchAnalogStart: Int?,
    val batchAnalogLength: Int?,
    val batchDigitalStart: Int?,
    val batchDigitalLength: Int?,
    val disabled: Boolean,
    val sortIndex: Int,
    val deviceTypeId: Long,
    val deviceTypeCode: String,
    val deviceTypeName: String,
    val tags: List<TagTreeNode>,
)

@Serializable
data class TagTreeNode(
    val id: Long,
    val name: String,
    val sortIndex: Int,
)

@Serializable
data class ProtocolCreateRequest(
    val name: String,
    val protocolTemplateId: Long,
    val pollingIntervalMs: Int,
    val transportConfig: ProtocolTransportConfig? = null,
    val sortIndex: Int = 0,
)

@Serializable
data class ProtocolUpdateRequest(
    val projectId: Long,
    val name: String,
    val protocolTemplateId: Long,
    val pollingIntervalMs: Int,
    val transportConfig: ProtocolTransportConfig? = null,
    val sortIndex: Int = 0,
)

@Serializable
data class ProtocolResponse(
    val id: Long,
    val name: String,
    val pollingIntervalMs: Int,
    val sortIndex: Int,
    val protocolTemplateId: Long,
    val transportConfig: ProtocolTransportConfig? = null,
)

@Serializable
data class ProtocolCatalogItemResponse(
    val id: Long,
    val name: String,
    val pollingIntervalMs: Int,
    val protocolTemplateId: Long,
    val protocolTemplateCode: String,
    val protocolTemplateName: String,
)

@Serializable
data class LinkExistingProtocolRequest(
    val protocolId: Long,
    val sortIndex: Int = 0,
)

@Serializable
data class ProtocolPositionUpdateRequest(
    val sourceProjectId: Long,
    val targetProjectId: Long,
    val sortIndex: Int = 0,
)

@Serializable
data class ModuleCreateRequest(
    val name: String,
    val moduleTemplateId: Long,
    val sortIndex: Int = 0,
)

@Serializable
data class ModuleUpdateRequest(
    val name: String,
    val moduleTemplateId: Long,
    val sortIndex: Int = 0,
)

@Serializable
data class ModuleResponse(
    val id: Long,
    val name: String,
    val protocolId: Long,
    val sortIndex: Int,
    val moduleTemplateId: Long,
)

@Serializable
data class ModulePositionUpdateRequest(
    val protocolId: Long? = null,
    val projectId: Long? = null,
    val sourceProjectId: Long? = null,
    val sortIndex: Int = 0,
)

@Serializable
data class DeviceCreateRequest(
    val name: String,
    val deviceTypeId: Long,
    val stationNo: Int,
    val requestIntervalMs: Int? = null,
    val writeIntervalMs: Int? = null,
    val byteOrder2: ByteOrder2? = null,
    val byteOrder4: ByteOrder4? = null,
    val floatOrder: FloatOrder? = null,
    val batchAnalogStart: Int? = null,
    val batchAnalogLength: Int? = null,
    val batchDigitalStart: Int? = null,
    val batchDigitalLength: Int? = null,
    val disabled: Boolean = false,
    val sortIndex: Int = 0,
)

@Serializable
data class DeviceUpdateRequest(
    val name: String,
    val deviceTypeId: Long,
    val stationNo: Int,
    val requestIntervalMs: Int? = null,
    val writeIntervalMs: Int? = null,
    val byteOrder2: ByteOrder2? = null,
    val byteOrder4: ByteOrder4? = null,
    val floatOrder: FloatOrder? = null,
    val batchAnalogStart: Int? = null,
    val batchAnalogLength: Int? = null,
    val batchDigitalStart: Int? = null,
    val batchDigitalLength: Int? = null,
    val disabled: Boolean = false,
    val sortIndex: Int = 0,
)

@Serializable
data class DeviceResponse(
    val id: Long,
    val name: String,
    val stationNo: Int,
    val requestIntervalMs: Int?,
    val writeIntervalMs: Int?,
    val byteOrder2: ByteOrder2?,
    val byteOrder4: ByteOrder4?,
    val floatOrder: FloatOrder?,
    val batchAnalogStart: Int?,
    val batchAnalogLength: Int?,
    val batchDigitalStart: Int?,
    val batchDigitalLength: Int?,
    val disabled: Boolean,
    val sortIndex: Int,
    val deviceTypeId: Long,
)

@Serializable
data class DevicePositionUpdateRequest(
    val moduleId: Long,
    val sortIndex: Int = 0,
)
