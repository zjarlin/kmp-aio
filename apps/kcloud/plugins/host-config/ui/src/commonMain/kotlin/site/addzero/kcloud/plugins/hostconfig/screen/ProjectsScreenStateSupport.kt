package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.ui.graphics.Color
import site.addzero.cupertino.workbench.components.field.CupertinoOption
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagValueTextInput
import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTemplateMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldKey
import site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.buildTransportConfig
import site.addzero.kcloud.plugins.hostconfig.api.template.defaultTransportConfig
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder2
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder4
import site.addzero.kcloud.plugins.hostconfig.model.enums.FloatOrder
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.PointType
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsScreenState
import site.addzero.kcloud.plugins.hostconfig.projects.displayName
import site.addzero.kcloud.plugins.hostconfig.projects.findModule
import site.addzero.kcloud.plugins.hostconfig.projects.findProtocol

/**
 * 解析当前创建spec。
 *
 * @param state 状态。
 */
internal fun resolveCurrentCreateSpec(
    state: ProjectsScreenState,
): CurrentCreateSpec {
    val selectedNode = state.selectedNode ?: return CurrentCreateSpec(
        enabled = false,
        hint = "请先在左侧选择一个节点。",
    )
    return when (selectedNode.kind) {
        HostConfigNodeKind.PROJECT -> {
            val candidates = resolveProjectProtocolCandidates(state, selectedNode.projectId)
            CurrentCreateSpec(
                node = selectedNode,
                actionType = NodeActionType.CREATE_DEVICE,
                enabled = candidates.isNotEmpty(),
                hint = if (candidates.isEmpty()) "当前工程还没有可承载设备的协议，请先关联协议。" else null,
            )
        }

        HostConfigNodeKind.PROTOCOL -> {
            CurrentCreateSpec(
                node = selectedNode,
                actionType = NodeActionType.CREATE_DEVICE,
                enabled = true,
            )
        }

        HostConfigNodeKind.DEVICE -> {
            val selectedDevice = state.selectedDevice
            val availableTemplates = selectedDevice?.let { device ->
                resolveModuleTemplatesForProtocol(state, device.protocolId)
            }.orEmpty()
            CurrentCreateSpec(
                node = selectedNode,
                actionType = NodeActionType.CREATE_MODULE,
                enabled = availableTemplates.isNotEmpty(),
                hint = when {
                    selectedDevice == null -> "当前设备上下文尚未加载完成。"
                    availableTemplates.isEmpty() -> "当前协议还没有可用模块模板。"
                    else -> null
                },
            )
        }

        HostConfigNodeKind.MODULE -> CurrentCreateSpec(
            node = selectedNode,
            actionType = NodeActionType.CREATE_TAG,
            enabled = true,
            hint = "标签从模块节点发起创建，会自动归到所属设备下。",
        )

        HostConfigNodeKind.TAG -> CurrentCreateSpec(
            enabled = false,
            hint = "当前节点没有默认的新建动作，请使用节点操作菜单。",
        )
    }
}

/**
 * 解析nodeactionmenu。
 *
 * @param state 状态。
 * @param node node。
 */
internal fun resolveNodeActionMenu(
    state: ProjectsScreenState,
    node: HostConfigTreeNode,
): NodeActionMenuSeed {
    val items = when (node.kind) {
        HostConfigNodeKind.PROJECT -> {
            val protocolCandidates = resolveProjectProtocolCandidates(state, node.projectId)
            val linkableProtocols = resolveLinkableProtocolTemplates(state, node.projectId)
            listOf(
                NodeActionItem(
                    type = NodeActionType.CREATE_DEVICE,
                    title = "新建设备",
                    enabled = protocolCandidates.isNotEmpty(),
                    note = if (protocolCandidates.isEmpty()) "请先关联协议" else null,
                ),
                NodeActionItem(
                    type = NodeActionType.LINK_PROTOCOL,
                    title = "关联协议",
                    enabled = linkableProtocols.isNotEmpty(),
                    note = if (linkableProtocols.isEmpty()) "当前工程已关联全部协议字典" else null,
                ),
                NodeActionItem(type = NodeActionType.DELETE, title = "删除", destructive = true),
            )
        }

        HostConfigNodeKind.PROTOCOL -> {
            listOf(
                NodeActionItem(
                    type = NodeActionType.CREATE_DEVICE,
                    title = "新建设备",
                ),
                NodeActionItem(type = NodeActionType.MOVE, title = "变更上级"),
                NodeActionItem(type = NodeActionType.DELETE, title = "删除", destructive = true),
            )
        }

        HostConfigNodeKind.MODULE -> listOf(
            NodeActionItem(type = NodeActionType.CREATE_TAG, title = "新建标签"),
            NodeActionItem(type = NodeActionType.MOVE, title = "变更上级"),
            NodeActionItem(type = NodeActionType.DELETE, title = "删除", destructive = true),
        )

        HostConfigNodeKind.DEVICE -> listOf(
            NodeActionItem(type = NodeActionType.CREATE_MODULE, title = "新建模块"),
            NodeActionItem(type = NodeActionType.MOVE, title = "变更上级"),
            NodeActionItem(type = NodeActionType.DELETE, title = "删除", destructive = true),
        )

        HostConfigNodeKind.TAG -> listOf(
            NodeActionItem(type = NodeActionType.MOVE, title = "变更上级"),
            NodeActionItem(type = NodeActionType.DELETE, title = "删除", destructive = true),
        )
    }
    return NodeActionMenuSeed(
        node = node,
        title = "${node.kind.label()}操作",
        subtitle = node.label,
        items = items,
    )
}

/**
 * 解析选中node类型。
 *
 * @param state 状态。
 */
internal fun resolveSelectedNodeKind(
    state: ProjectsScreenState,
): HostConfigNodeKind? {
    return state.selectedNode?.kind
        ?: state.selectedProject?.let { HostConfigNodeKind.PROJECT }
}

/**
 * 处理主机配置node类型。
 */
internal fun HostConfigNodeKind.summaryAccentColor(): Color {
    return when (this) {
        HostConfigNodeKind.PROJECT -> Color(0xFF2F80ED)
        HostConfigNodeKind.PROTOCOL -> Color(0xFF2E9E8E)
        HostConfigNodeKind.MODULE -> Color(0xFF3FA56B)
        HostConfigNodeKind.DEVICE -> Color(0xFFF29B38)
        HostConfigNodeKind.TAG -> Color(0xFF6B7A90)
    }
}

/**
 * 处理项目树响应。
 */
internal fun ProjectTreeResponse?.allModules(): List<ModuleTreeNode> {
    val project = this ?: return emptyList()
    return (project.modules + project.protocols.flatMap { protocol -> protocol.devices.flatMap { device -> device.modules } })
        .distinctBy { module -> module.id }
        .sortedWith(compareBy<ModuleTreeNode> { module -> module.sortIndex }.thenBy { module -> module.name })
}

/**
 * 表示创建项目seed。
 */
internal object CreateProjectSeed

/**
 * 表示关联协议seed。
 *
 * @property projectId 项目 ID。
 */
internal data class LinkProtocolSeed(
    val projectId: Long,
)

/**
 * 表示创建模块seed。
 *
 * @property projectId 项目 ID。
 * @property protocolId 协议 ID。
 * @property availableTemplates 可用模板。
 * @property protocolName 协议名称。
 * @property protocolTemplateName 协议模板名称。
 */
internal data class CreateModuleSeed(
    val projectId: Long,
    val deviceId: Long,
    val availableTemplates: List<ModuleTemplateOptionResponse> = emptyList(),
    val deviceName: String = "",
    val protocolId: Long,
    val protocolName: String = "",
    val protocolTemplateName: String = "",
)

/**
 * 表示选择协议 seed。
 *
 * @property projectId 项目 ID。
 * @property projectName 项目名称。
 * @property candidates candidates。
 */
internal data class ChooseProtocolSeed(
    val projectId: Long,
    val projectName: String,
    val title: String,
    val subtitle: String,
    val candidates: List<ProtocolCandidate>,
)

/**
 * 表示协议候选项。
 *
 * @property protocolId 协议 ID。
 * @property protocolName 协议名称。
 * @property protocolTemplateId 协议模板 ID。
 * @property protocolTemplateName 协议模板名称。
 * @property transportSummary 传输摘要。
 */
internal data class ProtocolCandidate(
    val protocolId: Long,
    val protocolName: String,
    val protocolTemplateId: Long,
    val protocolTemplateName: String,
    val transportSummary: String?,
)

/**
 * 表示创建设备seed。
 *
 * @property projectId 项目 ID。
 * @property protocolId 协议 ID。
 */
internal data class CreateDeviceSeed(
    val projectId: Long,
    val protocolId: Long,
    val protocolName: String = "",
    val protocolTemplateName: String = "",
)

/**
 * 表示创建标签seed。
 *
 * @property projectId 项目 ID。
 * @property deviceId 设备 ID。
 */
internal data class CreateTagSeed(
    val projectId: Long,
    val deviceId: Long,
)

/**
 * 表示移动nodeseed。
 *
 * @property node node。
 */
internal data class MoveNodeSeed(
    val node: HostConfigTreeNode,
)

/**
 * 表示nodeaction类型。
 */
internal enum class NodeActionType {
    CREATE_MODULE,
    LINK_PROTOCOL,
    CREATE_DEVICE,
    CREATE_TAG,
    MOVE,
    DELETE,
}

/**
 * 表示nodeactionmenuseed。
 *
 * @property node node。
 * @property title title。
 * @property subtitle subtitle。
 * @property items 条目列表。
 */
internal data class NodeActionMenuSeed(
    val node: HostConfigTreeNode,
    val title: String,
    val subtitle: String,
    val items: List<NodeActionItem>,
)

/**
 * 表示nodeactionitem。
 *
 * @property type 类型。
 * @property title title。
 * @property enabled 是否启用。
 * @property note note。
 * @property destructive destructive。
 */
internal data class NodeActionItem(
    val type: NodeActionType,
    val title: String,
    val enabled: Boolean = true,
    val note: String? = null,
    val destructive: Boolean = false,
)

/**
 * 表示当前创建spec。
 *
 * @property node node。
 * @property actionType action类型。
 * @property enabled 是否启用。
 * @property hint hint。
 */
internal data class CurrentCreateSpec(
    val node: HostConfigTreeNode? = null,
    val actionType: NodeActionType? = null,
    val enabled: Boolean,
    val hint: String? = null,
)

/**
 * 表示项目draft。
 *
 * @property name 名称。
 * @property description 描述。
 * @property remark 备注。
 * @property sortIndex 排序序号。
 */
internal data class ProjectDraft(
    val name: String = "",
    val description: String = "",
    val remark: String = "",
    val sortIndex: String = "0",
)

/**
 * 表示协议draft。
 *
 * @property name 名称。
 * @property protocolTemplateId 协议模板 ID。
 * @property pollingIntervalMs 轮询间隔（毫秒）。
 * @property sortIndex 排序序号。
 * @property host 主机地址。
 * @property tcpPort TCP端口。
 * @property portName 端口名。
 * @property baudRate 波特率。
 * @property dataBits 数据位。
 * @property stopBits 停止位。
 * @property parity 校验位。
 * @property responseTimeoutMs 响应超时时间（毫秒）。
 */
internal data class ProtocolDraft(
    val name: String = "",
    val protocolTemplateId: Long? = null,
    val pollingIntervalMs: String = "1000",
    val sortIndex: String = "0",
    val host: String = "",
    val tcpPort: String = "",
    val portName: String = "",
    val baudRate: String = "9600",
    val dataBits: String = "8",
    val stopBits: String = "1",
    val parity: Parity = Parity.NONE,
    val responseTimeoutMs: String = "1000",
)

/**
 * 表示模块draft。
 *
 * @property name 名称。
 * @property moduleTemplateId 模块模板 ID。
 * @property sortIndex 排序序号。
 */
internal data class ModuleDraft(
    val name: String = "",
    val moduleTemplateId: Long? = null,
    val sortIndex: String = "0",
)

/**
 * 表示设备draft。
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
internal data class DeviceDraft(
    val name: String = "",
    val deviceTypeId: Long? = null,
    val stationNo: String = "1",
    val requestIntervalMs: String = "",
    val writeIntervalMs: String = "",
    val byteOrder2: ByteOrder2? = null,
    val byteOrder4: ByteOrder4? = null,
    val floatOrder: FloatOrder? = null,
    val batchAnalogStart: String = "",
    val batchAnalogLength: String = "",
    val batchDigitalStart: String = "",
    val batchDigitalLength: String = "",
    val disabled: Boolean = false,
    val sortIndex: String = "0",
)

/**
 * 表示标签draft。
 *
 * @property name 名称。
 * @property description 描述。
 * @property dataTypeId 数据类型 ID。
 * @property registerTypeId 寄存器类型 ID。
 * @property registerAddress 寄存器地址。
 * @property enabled 是否启用。
 * @property defaultValue 默认值。
 * @property exceptionValue 异常值。
 * @property pointType 点位类型。
 * @property debounceMs 去抖毫秒。
 * @property sortIndex 排序序号。
 * @property scalingEnabled 缩放启用状态。
 * @property scalingOffset 缩放offset。
 * @property rawMin 原始min。
 * @property rawMax 原始max。
 * @property engMin 工程min。
 * @property engMax 工程max。
 * @property valueTexts 值文本。
 */
internal data class TagDraft(
    val name: String = "",
    val description: String = "",
    val dataTypeId: Long? = null,
    val registerTypeId: Long? = null,
    val registerAddress: String = "1",
    val enabled: Boolean = true,
    val forwardEnabled: Boolean = false,
    val forwardRegisterTypeId: Long? = null,
    val forwardRegisterAddress: String = "",
    val defaultValue: String = "",
    val exceptionValue: String = "",
    val pointType: PointType? = null,
    val debounceMs: String = "",
    val sortIndex: String = "0",
    val scalingEnabled: Boolean = false,
    val scalingOffset: String = "",
    val rawMin: String = "",
    val rawMax: String = "",
    val engMin: String = "",
    val engMax: String = "",
    val valueTexts: List<TagValueTextDraft> = defaultTagValueTextDrafts(),
)

/**
 * 表示标签值textdraft。
 *
 * @property rawValue 原始值。
 * @property displayText 显示文本。
 */
internal data class TagValueTextDraft(
    val rawValue: String,
    val displayText: String,
)

/**
 * 处理默认标签值textdrafts。
 */
internal fun defaultTagValueTextDrafts(): List<TagValueTextDraft> {
    return listOf(
        TagValueTextDraft(rawValue = "0", displayText = "Off"),
        TagValueTextDraft(rawValue = "1", displayText = "On"),
    )
}

/**
 * 构建项目linked协议请求。
 *
 * @param projectId 项目 ID。
 * @param template 模板。
 * @param sortIndex 目标排序序号。
 */
internal fun buildProjectLinkedProtocolRequest(
    projectId: Long,
    template: TemplateOptionResponse,
    sortIndex: Int,
): ProtocolCreateRequest {
    return ProtocolCreateRequest(
        name = buildProjectProtocolInstanceName(projectId, template),
        protocolTemplateId = template.id,
        pollingIntervalMs = 1000,
        transportConfig = template.defaultTransportConfig(),
        sortIndex = sortIndex,
    )
}

/**
 * 构建项目协议instance名称。
 *
 * @param projectId 项目 ID。
 * @param template 模板。
 */
internal fun buildProjectProtocolInstanceName(
    projectId: Long,
    template: TemplateOptionResponse,
): String {
    val normalizedCode = template.code.lowercase().replace('_', '-')
    return "project-$projectId-$normalizedCode"
}

/**
 * 处理模板选项响应。
 */
internal fun TemplateOptionResponse.defaultTransportConfig(): ProtocolTransportConfig? {
    return metadata?.defaultTransportConfig()
}

/**
 * 处理协议draft。
 *
 * @param template 协议模板。
 */
internal fun ProtocolDraft.toProtocolCreateRequest(
    template: TemplateOptionResponse,
): ProtocolCreateRequest {
    return ProtocolCreateRequest(
        name = name,
        protocolTemplateId = requireNotNull(protocolTemplateId),
        pollingIntervalMs = pollingIntervalMs.toIntOrNull() ?: 1000,
        transportConfig = toTransportConfig(template.metadata),
        sortIndex = sortIndex.toIntOrNull() ?: 0,
    )
}

/**
 * 处理协议draft。
 *
 * @param projectId 项目 ID。
 * @param existing existing。
 * @param template 协议模板。
 */
internal fun ProtocolDraft.toProtocolUpdateRequest(
    projectId: Long,
    existing: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode,
    template: TemplateOptionResponse?,
): ProtocolUpdateRequest {
    return ProtocolUpdateRequest(
        projectId = projectId,
        name = existing.name,
        protocolTemplateId = existing.protocolTemplateId,
        pollingIntervalMs = pollingIntervalMs.toIntOrNull() ?: 1000,
        transportConfig = toTransportConfig(template?.metadata),
        sortIndex = sortIndex.toIntOrNull() ?: 0,
    )
}

/**
 * 处理模块draft。
 */
internal fun ModuleDraft.toModuleCreateRequest(): ModuleCreateRequest {
    return ModuleCreateRequest(
        name = name,
        moduleTemplateId = requireNotNull(moduleTemplateId),
        sortIndex = sortIndex.toIntOrNull() ?: 0,
    )
}

/**
 * 处理模块draft。
 */
internal fun ModuleDraft.toModuleUpdateRequest(): ModuleUpdateRequest {
    return ModuleUpdateRequest(
        name = name,
        moduleTemplateId = requireNotNull(moduleTemplateId),
        sortIndex = sortIndex.toIntOrNull() ?: 0,
    )
}

/**
 * 处理设备draft。
 */
internal fun DeviceDraft.toDeviceCreateRequest(): DeviceCreateRequest {
    return DeviceCreateRequest(
        name = name,
        deviceTypeId = requireNotNull(deviceTypeId),
        stationNo = stationNo.toIntOrNull() ?: 1,
        requestIntervalMs = requestIntervalMs.toIntOrNull(),
        writeIntervalMs = writeIntervalMs.toIntOrNull(),
        byteOrder2 = byteOrder2,
        byteOrder4 = byteOrder4,
        floatOrder = floatOrder,
        batchAnalogStart = batchAnalogStart.toIntOrNull(),
        batchAnalogLength = batchAnalogLength.toIntOrNull(),
        batchDigitalStart = batchDigitalStart.toIntOrNull(),
        batchDigitalLength = batchDigitalLength.toIntOrNull(),
        disabled = disabled,
        sortIndex = sortIndex.toIntOrNull() ?: 0,
    )
}

/**
 * 处理设备draft。
 */
internal fun DeviceDraft.toDeviceUpdateRequest(): DeviceUpdateRequest {
    return DeviceUpdateRequest(
        name = name,
        deviceTypeId = requireNotNull(deviceTypeId),
        stationNo = stationNo.toIntOrNull() ?: 1,
        requestIntervalMs = requestIntervalMs.toIntOrNull(),
        writeIntervalMs = writeIntervalMs.toIntOrNull(),
        byteOrder2 = byteOrder2,
        byteOrder4 = byteOrder4,
        floatOrder = floatOrder,
        batchAnalogStart = batchAnalogStart.toIntOrNull(),
        batchAnalogLength = batchAnalogLength.toIntOrNull(),
        batchDigitalStart = batchDigitalStart.toIntOrNull(),
        batchDigitalLength = batchDigitalLength.toIntOrNull(),
        disabled = disabled,
        sortIndex = sortIndex.toIntOrNull() ?: 0,
    )
}

/**
 * 处理标签draft。
 */
internal fun TagDraft.toTagCreateRequest(): TagCreateRequest {
    return TagCreateRequest(
        name = name,
        description = description.ifBlank { null },
        dataTypeId = requireNotNull(dataTypeId),
        registerTypeId = requireNotNull(registerTypeId),
        registerAddress = registerAddress.toIntOrNull() ?: 0,
        enabled = enabled,
        forwardEnabled = forwardEnabled,
        forwardRegisterTypeId = forwardRegisterTypeId,
        forwardRegisterAddress = forwardRegisterAddress.toIntOrNull(),
        defaultValue = defaultValue.ifBlank { null },
        exceptionValue = exceptionValue.ifBlank { null },
        pointType = pointType,
        debounceMs = debounceMs.toIntOrNull(),
        sortIndex = sortIndex.toIntOrNull() ?: 0,
        scalingEnabled = scalingEnabled,
        scalingOffset = scalingOffset.ifBlank { null },
        rawMin = rawMin.ifBlank { null },
        rawMax = rawMax.ifBlank { null },
        engMin = engMin.ifBlank { null },
        engMax = engMax.ifBlank { null },
    )
}

/**
 * 处理标签draft。
 */
internal fun TagDraft.toTagUpdateRequest(): TagUpdateRequest {
    return TagUpdateRequest(
        name = name,
        description = description.ifBlank { null },
        dataTypeId = requireNotNull(dataTypeId),
        registerTypeId = requireNotNull(registerTypeId),
        registerAddress = registerAddress.toIntOrNull() ?: 0,
        enabled = enabled,
        forwardEnabled = forwardEnabled,
        forwardRegisterTypeId = forwardRegisterTypeId,
        forwardRegisterAddress = forwardRegisterAddress.toIntOrNull(),
        defaultValue = defaultValue.ifBlank { null },
        exceptionValue = exceptionValue.ifBlank { null },
        pointType = pointType,
        debounceMs = debounceMs.toIntOrNull(),
        sortIndex = sortIndex.toIntOrNull() ?: 0,
        scalingEnabled = scalingEnabled,
        scalingOffset = scalingOffset.ifBlank { null },
        rawMin = rawMin.ifBlank { null },
        rawMax = rawMax.ifBlank { null },
        engMin = engMin.ifBlank { null },
        engMax = engMax.ifBlank { null },
    )
}

/**
 * 处理标签draft。
 */
internal fun TagDraft.toTagValueTextInputs(): List<TagValueTextInput> {
    return valueTexts.mapIndexed { index, item ->
        TagValueTextInput(
            rawValue = item.rawValue,
            displayText = item.displayText,
            sortIndex = index,
        )
    }
}

/**
 * 处理site。
 */
internal fun site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse.toProjectDraft(): ProjectDraft {
    return ProjectDraft(
        name = name,
        description = description.orEmpty(),
        remark = remark.orEmpty(),
        sortIndex = sortIndex.toString(),
    )
}

/**
 * 处理site。
 */
internal fun site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode.toProtocolDraft(): ProtocolDraft {
    return ProtocolDraft(
        name = name,
        protocolTemplateId = protocolTemplateId,
        pollingIntervalMs = pollingIntervalMs.toString(),
        sortIndex = sortIndex.toString(),
        host = transportConfig?.host.orEmpty(),
        tcpPort = transportConfig?.tcpPort?.toString().orEmpty(),
        portName = transportConfig?.portName.orEmpty(),
        baudRate = transportConfig?.baudRate?.toString() ?: "9600",
        dataBits = transportConfig?.dataBits?.toString() ?: "8",
        stopBits = transportConfig?.stopBits?.toString() ?: "1",
        parity = transportConfig?.parity ?: Parity.NONE,
        responseTimeoutMs = transportConfig?.responseTimeoutMs?.toString() ?: "1000",
    )
}

/**
 * 处理site。
 */
internal fun site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode.toModuleDraft(): ModuleDraft {
    return ModuleDraft(
        name = name,
        moduleTemplateId = moduleTemplateId,
        sortIndex = sortIndex.toString(),
    )
}

/**
 * 处理site。
 */
internal fun site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode.toDeviceDraft(): DeviceDraft {
    return DeviceDraft(
        name = name,
        deviceTypeId = deviceTypeId,
        stationNo = stationNo.toString(),
        requestIntervalMs = requestIntervalMs?.toString().orEmpty(),
        writeIntervalMs = writeIntervalMs?.toString().orEmpty(),
        byteOrder2 = byteOrder2,
        byteOrder4 = byteOrder4,
        floatOrder = floatOrder,
        batchAnalogStart = batchAnalogStart?.toString().orEmpty(),
        batchAnalogLength = batchAnalogLength?.toString().orEmpty(),
        batchDigitalStart = batchDigitalStart?.toString().orEmpty(),
        batchDigitalLength = batchDigitalLength?.toString().orEmpty(),
        disabled = disabled,
        sortIndex = sortIndex.toString(),
    )
}

/**
 * 处理标签响应。
 */
internal fun TagResponse.toTagDraft(): TagDraft {
    return TagDraft(
        name = name,
        description = description.orEmpty(),
        dataTypeId = dataTypeId,
        registerTypeId = registerTypeId,
        registerAddress = registerAddress.toString(),
        enabled = enabled,
        forwardEnabled = forwardEnabled,
        forwardRegisterTypeId = forwardRegisterTypeId,
        forwardRegisterAddress = forwardRegisterAddress?.toString().orEmpty(),
        defaultValue = defaultValue.orEmpty(),
        exceptionValue = exceptionValue.orEmpty(),
        pointType = pointType,
        debounceMs = debounceMs?.toString().orEmpty(),
        sortIndex = sortIndex.toString(),
        scalingEnabled = scalingEnabled,
        scalingOffset = scalingOffset.orEmpty(),
        rawMin = rawMin.orEmpty(),
        rawMax = rawMax.orEmpty(),
        engMin = engMin.orEmpty(),
        engMax = engMax.orEmpty(),
        valueTexts = valueTexts.map { item ->
            TagValueTextDraft(
                rawValue = item.rawValue,
                displayText = item.displayText,
            )
        },
    )
}

/**
 * 处理项目draft。
 */
internal fun ProjectDraft.canSave(): Boolean {
    return name.isNotBlank()
}

/**
 * 处理协议draft。
 *
 * @param existing existing。
 */
internal fun ProtocolDraft.canSaveProtocol(
    existing: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode?,
): Boolean {
    return existing != null || (name.isNotBlank() && protocolTemplateId != null)
}

/**
 * 处理模块draft。
 */
internal fun ModuleDraft.canSave(): Boolean {
    return name.isNotBlank() && moduleTemplateId != null
}

/**
 * 处理设备draft。
 */
internal fun DeviceDraft.canSave(): Boolean {
    return name.isNotBlank() && deviceTypeId != null
}

/**
 * 处理标签draft。
 */
internal fun TagDraft.canSave(): Boolean {
    if (name.isBlank() || dataTypeId == null || registerTypeId == null) {
        return false
    }
    if (!forwardEnabled) {
        return true
    }
    return forwardRegisterTypeId != null && forwardRegisterAddress.toIntOrNull() != null
}

/**
 * 处理项目draft。
 */
internal fun ProjectDraft.toProjectCreateRequest(): ProjectCreateRequest {
    return ProjectCreateRequest(
        name = name,
        description = description.ifBlank { null },
        remark = remark.ifBlank { null },
        sortIndex = sortIndex.toIntOrNull() ?: 0,
    )
}

/**
 * 处理项目draft。
 */
internal fun ProjectDraft.toProjectUpdateRequest(): ProjectUpdateRequest {
    return ProjectUpdateRequest(
        name = name,
        description = description.ifBlank { null },
        remark = remark.ifBlank { null },
        sortIndex = sortIndex.toIntOrNull() ?: 0,
    )
}

/**
 * 处理协议draft。
 *
 * @param metadata 协议模板元数据。
 */
internal fun ProtocolDraft.toTransportConfig(
    metadata: ProtocolTemplateMetadataResponse?,
): ProtocolTransportConfig? {
    return metadata?.buildTransportConfig { key ->
        transportFieldValue(key)
    }
}

/**
 * 读取协议draft里的传输字段值。
 *
 * @param key 字段键。
 */
internal fun ProtocolDraft.transportFieldValue(
    key: ProtocolTransportFieldKey,
): String {
    return when (key) {
        ProtocolTransportFieldKey.HOST -> host
        ProtocolTransportFieldKey.TCP_PORT -> tcpPort
        ProtocolTransportFieldKey.PORT_NAME -> portName
        ProtocolTransportFieldKey.BAUD_RATE -> baudRate
        ProtocolTransportFieldKey.DATA_BITS -> dataBits
        ProtocolTransportFieldKey.STOP_BITS -> stopBits
        ProtocolTransportFieldKey.PARITY -> parity.name
        ProtocolTransportFieldKey.RESPONSE_TIMEOUT_MS -> responseTimeoutMs
    }
}

/**
 * 更新协议draft里的传输字段值。
 *
 * @param key 字段键。
 * @param value 字段值。
 */
internal fun ProtocolDraft.withTransportFieldValue(
    key: ProtocolTransportFieldKey,
    value: String,
): ProtocolDraft {
    return when (key) {
        ProtocolTransportFieldKey.HOST -> copy(host = value)
        ProtocolTransportFieldKey.TCP_PORT -> copy(tcpPort = value)
        ProtocolTransportFieldKey.PORT_NAME -> copy(portName = value)
        ProtocolTransportFieldKey.BAUD_RATE -> copy(baudRate = value)
        ProtocolTransportFieldKey.DATA_BITS -> copy(dataBits = value)
        ProtocolTransportFieldKey.STOP_BITS -> copy(stopBits = value)
        ProtocolTransportFieldKey.PARITY -> {
            copy(parity = Parity.entries.firstOrNull { option -> option.name == value } ?: Parity.NONE)
        }

        ProtocolTransportFieldKey.RESPONSE_TIMEOUT_MS -> copy(responseTimeoutMs = value)
    }
}

/**
 * 处理主机配置node类型。
 */
internal fun HostConfigNodeKind.label(): String {
    return when (this) {
        HostConfigNodeKind.PROJECT -> "工程"
        HostConfigNodeKind.PROTOCOL -> "协议"
        HostConfigNodeKind.MODULE -> "模块"
        HostConfigNodeKind.DEVICE -> "设备"
        HostConfigNodeKind.TAG -> "标签"
    }
}
