package site.addzero.kcloud.plugins.hostconfig.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import site.addzero.kcloud.plugins.hostconfig.api.project.ModulePositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.routes.common.ConflictException

/**
 * 验证项目服务相关场景。
 */
class ProjectServiceTest {

    @Test
    /**
     * 处理should更新模块withoutunloaded协议模板。
     */
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
    /**
     * 处理should移动模块across项目withoutunloaded模块模板协议模板。
     */
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

    @Test
    /**
     * 处理shouldrejectduplicate协议模板inside项目。
     */
    fun shouldRejectDuplicateProtocolTemplateInsideProject() {
        ProjectServiceTestFixture().use { fixture ->
            val project = fixture.createProject("测试工程")
            fixture.createProtocol(project.id, "协议A")

            val error =
                assertFailsWith<ConflictException> {
                    fixture.createProtocol(project.id, "协议B")
                }

            assertEquals("Project already links a protocol for this template", error.message)
        }
    }

    @Test
    /**
     * 处理shouldrejectmoving协议into项目withsame模板。
     */
    fun shouldRejectMovingProtocolIntoProjectWithSameTemplate() {
        ProjectServiceTestFixture().use { fixture ->
            val sourceProject = fixture.createProject("来源工程")
            val targetProject = fixture.createProject("目标工程")
            val protocol = fixture.createProtocol(sourceProject.id, "来源协议")
            fixture.createProtocol(targetProject.id, "目标协议")

            val error =
                assertFailsWith<ConflictException> {
                    fixture.service.updateProtocolPosition(
                        protocolId = protocol.id,
                        request =
                            ProtocolPositionUpdateRequest(
                                sourceProjectId = sourceProject.id,
                                targetProjectId = targetProject.id,
                                sortIndex = 0,
                            ),
                    )
                }

            assertEquals("Project already links a protocol for this template", error.message)
        }
    }
}
