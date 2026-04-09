package site.addzero.kcloud.plugins.hostconfig.screen

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

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
                protocolId = 21,
                sortIndex = 0,
                moduleTemplateId = 31,
                moduleTemplateCode = "template-1",
                moduleTemplateName = "模板1",
                devices = emptyList(),
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
                            modules = listOf(repeatedModule),
                        ),
                    ),
                modules = listOf(repeatedModule),
            )

        val modules = project.allModules()

        assertEquals(1, modules.size)
        assertEquals(11, modules.single().id)
    }
}
