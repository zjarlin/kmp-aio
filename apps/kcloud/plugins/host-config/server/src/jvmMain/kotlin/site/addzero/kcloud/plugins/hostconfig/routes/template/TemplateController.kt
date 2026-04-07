package site.addzero.kcloud.plugins.hostconfig.api.template

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.hostconfig.service.TemplateService

@RestController
@RequestMapping("/api/v1/templates")
class TemplateController(
    private val templateService: TemplateService,
) {

    @GetMapping("/protocols")
    fun listProtocolTemplates(): List<TemplateOptionResponse> =
        templateService.listProtocolTemplates()

    @GetMapping("/modules")
    fun listModuleTemplates(
        @RequestParam protocolTemplateId: Long,
    ): List<ModuleTemplateOptionResponse> =
        templateService.listModuleTemplates(protocolTemplateId)

    @GetMapping("/device-types")
    fun listDeviceTypes(): List<TemplateOptionResponse> =
        templateService.listDeviceTypes()

    @GetMapping("/register-types")
    fun listRegisterTypes(): List<TemplateOptionResponse> =
        templateService.listRegisterTypes()

    @GetMapping("/data-types")
    fun listDataTypes(): List<TemplateOptionResponse> =
        templateService.listDataTypes()
}
