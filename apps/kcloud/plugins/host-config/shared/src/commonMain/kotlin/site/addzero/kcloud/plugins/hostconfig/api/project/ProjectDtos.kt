package site.addzero.kcloud.plugins.hostconfig.api.project

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder2
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder4
import site.addzero.kcloud.plugins.hostconfig.model.enums.FloatOrder
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Serializable
/**
 * 表示项目创建请求参数。
 *
 * @property name 名称。
 * @property description 描述。
 * @property remark 备注。
 * @property sortIndex 排序序号。
 */
data class ProjectCreateRequest(
    val name: String,
    val description: String? = null,
    val remark: String? = null,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示项目更新请求参数。
 *
 * @property name 名称。
 * @property description 描述。
 * @property remark 备注。
 * @property sortIndex 排序序号。
 */
data class ProjectUpdateRequest(
    val name: String,
    val description: String? = null,
    val remark: String? = null,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示项目响应结果。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property description 描述。
 * @property remark 备注。
 * @property sortIndex 排序序号。
 * @property createdAt 创建时间戳。
 * @property updatedAt 更新时间戳。
 */
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
/**
 * 表示项目位置更新请求参数。
 *
 * @property sortIndex 排序序号。
 */
data class ProjectPositionUpdateRequest(
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示项目树响应结果。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property description 描述。
 * @property remark 备注。
 * @property sortIndex 排序序号。
 * @property protocols 协议。
 * @property modules 模块聚合视图。
 */
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
/**
 * 表示协议传输配置。
 *
 * @property transportType 传输类型。
 * @property host 主机地址。
 * @property tcpPort TCP端口。
 * @property portName 端口名。
 * @property baudRate 波特率。
 * @property dataBits 数据位。
 * @property stopBits 停止位。
 * @property parity 校验位。
 * @property responseTimeoutMs 响应超时时间（毫秒）。
 */
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
/**
 * 表示协议树node。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property pollingIntervalMs 轮询间隔（毫秒）。
 * @property sortIndex 排序序号。
 * @property protocolTemplateId 协议模板 ID。
 * @property protocolTemplateCode 协议模板编码。
 * @property protocolTemplateName 协议模板名称。
 * @property transportConfig 传输配置。
 * @property devices 设备。
 */
data class ProtocolTreeNode(
    val id: Long,
    val name: String,
    val pollingIntervalMs: Int,
    val sortIndex: Int,
    val protocolTemplateId: Long,
    val protocolTemplateCode: String,
    val protocolTemplateName: String,
    val transportConfig: ProtocolTransportConfig? = null,
    val devices: List<DeviceTreeNode>,
)

@Serializable
/**
 * 表示模块树node。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property deviceId 设备 ID。
 * @property protocolId 协议 ID。
 * @property sortIndex 排序序号。
 * @property moduleTemplateId 模块模板 ID。
 * @property moduleTemplateCode 模块模板编码。
 * @property moduleTemplateName 模块模板名称。
 */
data class ModuleTreeNode(
    val id: Long,
    val name: String,
    val deviceId: Long,
    val protocolId: Long,
    val sortIndex: Int,
    val moduleTemplateId: Long,
    val moduleTemplateCode: String,
    val moduleTemplateName: String,
)

@Serializable
/**
 * 表示设备树node。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property stationNo stationno。
 * @property requestIntervalMs 请求间隔（毫秒）。
 * @property writeIntervalMs 写入间隔（毫秒）。
 * @property byteOrder2 双字节字节序。
 * @property byteOrder4 四字节字节序。
 * @property floatOrder 浮点字序。
 * @property batchAnalogStart batchanalog开始。
 * @property batchAnalogLength batchanaloglength。
 * @property batchDigitalStart batchdigital开始。
 * @property batchDigitalLength batchdigitallength。
 * @property disabled disabled。
 * @property sortIndex 排序序号。
 * @property protocolId 协议 ID。
 * @property deviceTypeId 设备类型 ID。
 * @property deviceTypeCode 设备类型编码。
 * @property deviceTypeName 设备类型名称。
 * @property modules 模块。
 * @property tags 标签。
 */
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
    val protocolId: Long,
    val deviceTypeId: Long,
    val deviceTypeCode: String,
    val deviceTypeName: String,
    val modules: List<ModuleTreeNode>,
    val tags: List<TagTreeNode>,
)

@Serializable
/**
 * 表示标签树node。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property sortIndex 排序序号。
 */
data class TagTreeNode(
    val id: Long,
    val name: String,
    val sortIndex: Int,
)

@Serializable
/**
 * 表示协议创建请求参数。
 *
 * @property name 名称。
 * @property protocolTemplateId 协议模板 ID。
 * @property pollingIntervalMs 轮询间隔（毫秒）。
 * @property transportConfig 传输配置。
 * @property sortIndex 排序序号。
 */
data class ProtocolCreateRequest(
    val name: String,
    val protocolTemplateId: Long,
    val pollingIntervalMs: Int,
    val transportConfig: ProtocolTransportConfig? = null,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示协议更新请求参数。
 *
 * @property projectId 项目 ID。
 * @property name 名称。
 * @property protocolTemplateId 协议模板 ID。
 * @property pollingIntervalMs 轮询间隔（毫秒）。
 * @property transportConfig 传输配置。
 * @property sortIndex 排序序号。
 */
data class ProtocolUpdateRequest(
    val projectId: Long,
    val name: String,
    val protocolTemplateId: Long,
    val pollingIntervalMs: Int,
    val transportConfig: ProtocolTransportConfig? = null,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示协议响应结果。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property pollingIntervalMs 轮询间隔（毫秒）。
 * @property sortIndex 排序序号。
 * @property protocolTemplateId 协议模板 ID。
 * @property transportConfig 传输配置。
 */
data class ProtocolResponse(
    val id: Long,
    val name: String,
    val pollingIntervalMs: Int,
    val sortIndex: Int,
    val protocolTemplateId: Long,
    val transportConfig: ProtocolTransportConfig? = null,
)

@Serializable
/**
 * 表示协议目录item响应结果。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property pollingIntervalMs 轮询间隔（毫秒）。
 * @property protocolTemplateId 协议模板 ID。
 * @property protocolTemplateCode 协议模板编码。
 * @property protocolTemplateName 协议模板名称。
 */
data class ProtocolCatalogItemResponse(
    val id: Long,
    val name: String,
    val pollingIntervalMs: Int,
    val protocolTemplateId: Long,
    val protocolTemplateCode: String,
    val protocolTemplateName: String,
)

@Serializable
/**
 * 表示关联existing协议请求参数。
 *
 * @property protocolId 协议 ID。
 * @property sortIndex 排序序号。
 */
data class LinkExistingProtocolRequest(
    val protocolId: Long,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示协议位置更新请求参数。
 *
 * @property sourceProjectId 来源项目 ID。
 * @property targetProjectId 目标项目 ID。
 * @property sortIndex 排序序号。
 */
data class ProtocolPositionUpdateRequest(
    val sourceProjectId: Long,
    val targetProjectId: Long,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示模块创建请求参数。
 *
 * @property name 名称。
 * @property moduleTemplateId 模块模板 ID。
 * @property sortIndex 排序序号。
 */
data class ModuleCreateRequest(
    val name: String,
    val moduleTemplateId: Long,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示模块更新请求参数。
 *
 * @property name 名称。
 * @property moduleTemplateId 模块模板 ID。
 * @property sortIndex 排序序号。
 */
data class ModuleUpdateRequest(
    val name: String,
    val moduleTemplateId: Long,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示模块响应结果。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property deviceId 设备 ID。
 * @property protocolId 协议 ID。
 * @property sortIndex 排序序号。
 * @property moduleTemplateId 模块模板 ID。
 */
data class ModuleResponse(
    val id: Long,
    val name: String,
    val deviceId: Long,
    val protocolId: Long,
    val sortIndex: Int,
    val moduleTemplateId: Long,
)

@Serializable
/**
 * 表示模块位置更新请求参数。
 *
 * @property deviceId 设备 ID。
 * @property sortIndex 排序序号。
 */
data class ModulePositionUpdateRequest(
    val deviceId: Long,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示设备创建请求参数。
 *
 * @property name 名称。
 * @property deviceTypeId 设备类型 ID。
 * @property stationNo stationno。
 * @property requestIntervalMs 请求间隔（毫秒）。
 * @property writeIntervalMs 写入间隔（毫秒）。
 * @property byteOrder2 双字节字节序。
 * @property byteOrder4 四字节字节序。
 * @property floatOrder 浮点字序。
 * @property batchAnalogStart batchanalog开始。
 * @property batchAnalogLength batchanaloglength。
 * @property batchDigitalStart batchdigital开始。
 * @property batchDigitalLength batchdigitallength。
 * @property disabled disabled。
 * @property sortIndex 排序序号。
 */
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
/**
 * 表示设备更新请求参数。
 *
 * @property name 名称。
 * @property deviceTypeId 设备类型 ID。
 * @property stationNo stationno。
 * @property requestIntervalMs 请求间隔（毫秒）。
 * @property writeIntervalMs 写入间隔（毫秒）。
 * @property byteOrder2 双字节字节序。
 * @property byteOrder4 四字节字节序。
 * @property floatOrder 浮点字序。
 * @property batchAnalogStart batchanalog开始。
 * @property batchAnalogLength batchanaloglength。
 * @property batchDigitalStart batchdigital开始。
 * @property batchDigitalLength batchdigitallength。
 * @property disabled disabled。
 * @property sortIndex 排序序号。
 */
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
/**
 * 表示设备响应结果。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property stationNo stationno。
 * @property requestIntervalMs 请求间隔（毫秒）。
 * @property writeIntervalMs 写入间隔（毫秒）。
 * @property byteOrder2 双字节字节序。
 * @property byteOrder4 四字节字节序。
 * @property floatOrder 浮点字序。
 * @property batchAnalogStart batchanalog开始。
 * @property batchAnalogLength batchanaloglength。
 * @property batchDigitalStart batchdigital开始。
 * @property batchDigitalLength batchdigitallength。
 * @property disabled disabled。
 * @property sortIndex 排序序号。
 * @property protocolId 协议 ID。
 * @property deviceTypeId 设备类型 ID。
 */
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
    val protocolId: Long,
    val deviceTypeId: Long,
)

@Serializable
/**
 * 表示设备位置更新请求参数。
 *
 * @property protocolId 协议 ID。
 * @property sortIndex 排序序号。
 */
data class DevicePositionUpdateRequest(
    val protocolId: Long,
    val sortIndex: Int = 0,
)
