package site.addzero.coding.playground.demo.beta

import org.koin.core.annotation.Single
import site.addzero.coding.playground.demo.alpha.DemoAlphaService

@Single
class DemoBetaService(
    private val alphaService: DemoAlphaService,
) {
    fun marker(): String = "beta:${alphaService.marker()}"
}
