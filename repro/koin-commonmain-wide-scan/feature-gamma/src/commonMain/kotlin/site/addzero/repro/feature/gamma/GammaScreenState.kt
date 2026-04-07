package site.addzero.repro.feature.gamma

import org.koin.core.annotation.Single
import site.addzero.repro.feature.beta.BetaService

@Single
class GammaScreenState(
    private val betaService: BetaService,
) {
    fun label(): String = "gamma(${betaService.label()})"
}
