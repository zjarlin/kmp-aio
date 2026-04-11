package site.addzero.kcloud.plugins.hostconfig.screen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTemplateMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldKey
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldWidget
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFormMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType
import site.addzero.kcloud.plugins.hostconfig.projects.findModule
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsScreenState

/**
 * 验证项目界面状态辅助逻辑。
 */
class ProjectsScreenStateSupportTest {

    @Test
    /**
     * 处理allmodules去重重复模块。
     */
    fun shouldDeduplicateRepeatedModulesWhenProjectAggregateAndProtocolModulesOverlap() {
        val repeatedModule =
            ModuleTreeNode(
                id = 11,
                name = "模块1",
                deviceId = 12,
                protocolId = 21,
                sortIndex = 0,
                moduleTemplateId = 31,
                moduleTemplateCode = "template-1",
                moduleTemplateName = "模板1",
            )
        val device =
            DeviceTreeNode(
                id = 12,
                name = "设备1",
                stationNo = 1,
                requestIntervalMs = null,
                writeIntervalMs = null,
                byteOrder2 = null,
                byteOrder4 = null,
                floatOrder = null,
                batchAnalogStart = null,
                batchAnalogLength = null,
                batchDigitalStart = null,
                batchDigitalLength = null,
                disabled = false,
                sortIndex = 0,
                protocolId = 21,
                deviceTypeId = 51,
                deviceTypeCode = "device-type-1",
                deviceTypeName = "设备类型1",
                modules = listOf(repeatedModule),
                tags = emptyList(),
            )
        val project =
            ProjectTreeResponse(
                id = 1,
                name = "工程1",
                description = null,
                remark = null,
                sortIndex = 0,
                protocols =
                    listOf(
                        ProtocolTreeNode(
                            id = 21,
                            name = "协议1",
                            pollingIntervalMs = 1000,
                            sortIndex = 0,
                            protocolTemplateId = 41,
                            protocolTemplateCode = "protocol-template-1",
                            protocolTemplateName = "协议模板1",
                            transportConfig =
                                ProtocolTransportConfig(
                                    transportType = TransportType.TCP,
                                ),
                            devices = listOf(device),
                        ),
                    ),
                modules = listOf(repeatedModule),
            )

        val modules = project.allModules()

        assertEquals(1, modules.size)
        assertEquals(11, modules.single().id)
    }

    @Test
    /**
     * 处理should从设备层级找到模块。
     */
    fun shouldFindModuleThroughDeviceHierarchy() {
        val module =
            ModuleTreeNode(
                id = 31,
                name = "模块A",
                deviceId = 21,
                protocolId = 11,
                sortIndex = 0,
                moduleTemplateId = 41,
                moduleTemplateCode = "template-a",
                moduleTemplateName = "模板A",
            )
        val device =
            DeviceTreeNode(
                id = 21,
                name = "设备A",
                stationNo = 1,
                requestIntervalMs = null,
                writeIntervalMs = null,
                byteOrder2 = null,
                byteOrder4 = null,
                floatOrder = null,
                batchAnalogStart = null,
                batchAnalogLength = null,
                batchDigitalStart = null,
                batchDigitalLength = null,
                disabled = false,
                sortIndex = 0,
                protocolId = 11,
                deviceTypeId = 51,
                deviceTypeCode = "device-type-a",
                deviceTypeName = "设备类型A",
                modules = listOf(module),
                tags = emptyList(),
            )
        val projectTrees =
            listOf(
                ProjectTreeResponse(
                    id = 1,
                    name = "工程A",
                    description = null,
                    remark = null,
                    sortIndex = 0,
                    protocols =
                        listOf(
                            ProtocolTreeNode(
                                id = 11,
                                name = "协议A",
                                pollingIntervalMs = 1000,
                                sortIndex = 0,
                                protocolTemplateId = 61,
                                protocolTemplateCode = "protocol-template-a",
                                protocolTemplateName = "协议模板A",
                                transportConfig = ProtocolTransportConfig(transportType = TransportType.TCP),
                                devices = listOf(device),
                            ),
                        ),
                    modules = emptyList(),
                ),
            )

        val found = projectTrees.findModule(module.id)

        assertNotNull(found)
        assertEquals(module.id, found.id)
        assertEquals(device.id, found.deviceId)
        assertEquals(device.protocolId, found.protocolId)
    }

    @Test
    /**
     * 处理协议模板元数据默认传输配置。
     */
    fun shouldBuildDefaultTransportConfigFromTemplateMetadata() {
        val template =
            TemplateOptionResponse(
                id = 1,
                code = "MODBUS_RTU_CLIENT",
                name = "ModbusRTU",
                description = null,
                sortIndex = 0,
                metadata = buildRtuMetadata(),
            )

        val config = template.defaultTransportConfig()

        assertEquals(TransportType.RTU, config?.transportType)
        assertEquals("9600", config?.baudRate?.toString())
        assertEquals("8", config?.dataBits?.toString())
        assertEquals("1", config?.stopBits?.toString())
        assertEquals("1000", config?.responseTimeoutMs?.toString())
    }

    @Test
    /**
     * 处理协议draft按照元数据字段映射传输配置。
     */
    fun shouldMapProtocolDraftToTransportConfigUsingMetadata() {
        val draft =
            ProtocolDraft(
                portName = "COM4",
                baudRate = "115200",
                dataBits = "7",
                stopBits = "2",
                responseTimeoutMs = "20",
            )

        val config = draft.toTransportConfig(buildRtuMetadata())

        assertEquals(TransportType.RTU, config?.transportType)
        assertEquals("COM4", config?.portName)
        assertEquals("115200", config?.baudRate?.toString())
        assertEquals("7", config?.dataBits?.toString())
        assertEquals("2", config?.stopBits?.toString())
        assertEquals("20", config?.responseTimeoutMs?.toString())
    }

    @Test
    /**
     * 处理协议摘要跟随元数据字段。
     */
    fun shouldRenderTransportSummaryFromMetadataSummaryKeys() {
        val config =
            ProtocolTransportConfig(
                transportType = TransportType.RTU,
                portName = "COM4",
                baudRate = 9600,
            )

        val summary = config.toSummary(buildRtuMetadata())

        assertEquals("COM4 / 9600 / -", summary)
    }

    @Test
    /**
     * 处理标签创建入口只保留在模块节点。
     */
    fun shouldOnlyExposeCreateTagActionOnModuleNode() {
        val moduleNode =
            HostConfigTreeNode(
                id = "module:31",
                kind = HostConfigNodeKind.MODULE,
                projectId = 1,
                entityId = 31,
                parentEntityId = 21,
                label = "模块A",
            )
        val deviceNode =
            HostConfigTreeNode(
                id = "device:21",
                kind = HostConfigNodeKind.DEVICE,
                projectId = 1,
                entityId = 21,
                parentEntityId = 11,
                label = "设备A",
            )
        val state =
            ProjectsScreenState(
                treeNodes = listOf(moduleNode, deviceNode),
                selectedNodeId = moduleNode.id,
            )

        val createSpec = resolveCurrentCreateSpec(state)
        val moduleMenu = resolveNodeActionMenu(state, moduleNode)
        val deviceMenu = resolveNodeActionMenu(state, deviceNode)

        assertEquals(NodeActionType.CREATE_TAG, createSpec.actionType)
        assertEquals(true, createSpec.enabled)
        assertEquals(true, moduleMenu.items.any { item -> item.type == NodeActionType.CREATE_TAG })
        assertEquals(false, deviceMenu.items.any { item -> item.type == NodeActionType.CREATE_TAG })
    }

    @Test
    /**
     * 处理移动上级候选按目标值去重。
     */
    fun shouldDeduplicateMoveOptionsByTargetValue() {
        val currentProtocol =
            ProtocolTreeNode(
                id = 11,
                name = "协议A",
                pollingIntervalMs = 1000,
                sortIndex = 0,
                protocolTemplateId = 101,
                protocolTemplateCode = "protocol-a",
                protocolTemplateName = "协议模板A",
                transportConfig = ProtocolTransportConfig(transportType = TransportType.TCP),
                devices = emptyList(),
            )
        val state =
            ProjectsScreenState(
                projects = listOf(
                    ProjectResponse(1, "工程A", null, null, 0, 0, 0),
                    ProjectResponse(2, "工程B", null, null, 0, 0, 0),
                    ProjectResponse(2, "工程B", null, null, 0, 0, 0),
                ),
                projectTrees = listOf(
                    ProjectTreeResponse(
                        id = 1,
                        name = "工程A",
                        description = null,
                        remark = null,
                        sortIndex = 0,
                        protocols = listOf(currentProtocol),
                        modules = emptyList(),
                    ),
                    ProjectTreeResponse(
                        id = 2,
                        name = "工程B",
                        description = null,
                        remark = null,
                        sortIndex = 0,
                        protocols = emptyList(),
                        modules = emptyList(),
                    ),
                ),
            )
        val node =
            HostConfigTreeNode(
                id = "protocol:11",
                kind = HostConfigNodeKind.PROTOCOL,
                projectId = 1,
                entityId = 11,
                label = "协议A",
            )

        val options = resolveMoveOptions(state, node)

        assertEquals(listOf("project:1", "project:2"), options.map { option -> option.value })
    }
}

/**
 * 构建 RTU 测试元数据。
 */
private fun buildRtuMetadata(): ProtocolTemplateMetadataResponse {
    return ProtocolTemplateMetadataResponse(
        transportType = TransportType.RTU,
        transportForm =
            ProtocolTransportFormMetadataResponse(
                summaryKeys = listOf(
                    ProtocolTransportFieldKey.PORT_NAME,
                    ProtocolTransportFieldKey.BAUD_RATE,
                    ProtocolTransportFieldKey.PARITY,
                ),
                fields = listOf(
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.PORT_NAME,
                        label = "串口",
                    ),
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.BAUD_RATE,
                        label = "波特率",
                        widget = ProtocolTransportFieldWidget.SELECT,
                        defaultValue = "9600",
                        options = listOf(
                            ProtocolTransportFieldOptionResponse("9600", "9600"),
                            ProtocolTransportFieldOptionResponse("115200", "115200"),
                        ),
                    ),
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.DATA_BITS,
                        label = "数据位",
                        widget = ProtocolTransportFieldWidget.SELECT,
                        defaultValue = "8",
                        options = listOf(
                            ProtocolTransportFieldOptionResponse("7", "7"),
                            ProtocolTransportFieldOptionResponse("8", "8"),
                        ),
                    ),
                    ProtocolTransportFieldMetadataResponse(
                        key = ProtocolTransportFieldKey.STOP_BITS,
                        label = "停止位",
                        widget = ProtocolTransportFieldWidget.SELECT,
                        defaultValue = "1",
                        options = listOf(
                            ProtocolTransportFieldOptionResponse("1", "1"),
                            ProtocolTransportFieldOptionResponse("2", "2"),
                        ),
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
