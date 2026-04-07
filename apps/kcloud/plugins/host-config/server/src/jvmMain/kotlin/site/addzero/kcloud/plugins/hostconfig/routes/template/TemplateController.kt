package site.addzero.kcloud.plugins.hostconfig.routes.template

import org.koin.core.annotation.Single
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.service.TemplateService

/**
 * 宿主配置里的内置协议模板目录路由。
 */
@Single
@RestController
@RequestMapping("/api/host-config/v1/templates")
class TemplateController(
    private val templateService: TemplateService,
) {
    /** 返回可选协议模板，用于协议页只读目录展示。 */
    @GetMapping("/protocols")
    fun listProtocolTemplates(): List<TemplateOptionResponse> =
        templateService.listProtocolTemplates()

    /** 按协议模板读取模块模板目录，供工程编辑时选择模块类型。 */
    @GetMapping("/modules")
    fun listModuleTemplates(
        @RequestParam("protocolTemplateId") protocolTemplateId: Long,
    ): List<ModuleTemplateOptionResponse> =
        templateService.listModuleTemplates(protocolTemplateId)

    /** 返回设备类型目录，供模块下设备建模时选择。 */
    @GetMapping("/device-types")
    fun listDeviceTypes(): List<TemplateOptionResponse> =
        templateService.listDeviceTypes()

    /** 返回寄存器类型目录，供点位建模时选择。 */
    @GetMapping("/register-types")
    fun listRegisterTypes(): List<TemplateOptionResponse> =
        templateService.listRegisterTypes()

    /** 返回数据类型目录，供点位解析与缩放配置使用。 */
    @GetMapping("/data-types")
    fun listDataTypes(): List<TemplateOptionResponse> =
        templateService.listDataTypes()
}
