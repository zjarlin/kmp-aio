package site.addzero.kcloud.plugins.hostconfig.service

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.kcloud.plugins.hostconfig.api.project.ModulePositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleUpdateRequest

class ProjectServiceTest {

    @Test
    fun shouldUpdateModuleWithoutUnloadedProtocolTemplate() {
        ProjectServiceTestFixture().use { fixture ->
            val project = fixture.createProject("测试工程A")
            val protocol = fixture.createProtocol(project.id, "测试协议A")
            val module = fixture.createModule(protocol.id, "测试模块A")

            val updated =
                fixture.service.updateModule(
                    moduleId = module.id,
                    request =
                        ModuleUpdateRequest(
                            name = "模块1",
                            moduleTemplateId = 1,
                            sortIndex = 0,
                        ),
                )

            assertEquals(module.id, updated.id)
            assertEquals("模块1", updated.name)
            assertEquals(protocol.id, updated.protocolId)
            assertEquals(0, updated.sortIndex)
        }
    }

    @Test
    fun shouldMoveModuleAcrossProjectsWithoutUnloadedModuleTemplateProtocolTemplate() {
        ProjectServiceTestFixture().use { fixture ->
            val sourceProject = fixture.createProject("测试工程源")
            val targetProject = fixture.createProject("测试工程目标")
            val sourceProtocol = fixture.createProtocol(sourceProject.id, "测试协议源")
            val targetProtocol = fixture.createProtocol(targetProject.id, "测试协议目标")
            val module = fixture.createModule(sourceProtocol.id, "测试模块迁移")

            val moved =
                fixture.service.updateModulePosition(
                    moduleId = module.id,
                    request =
                        ModulePositionUpdateRequest(
                            projectId = targetProject.id,
                            sourceProjectId = sourceProject.id,
                            sortIndex = 0,
                        ),
                )

            assertEquals(module.id, moved.id)
            assertEquals(targetProtocol.id, moved.protocolId)
            assertEquals(1L, moved.moduleTemplateId)
            assertEquals(0, moved.sortIndex)
        }
    }
}
