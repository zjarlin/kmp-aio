package site.addzero.repro.feature.beta

import org.koin.core.annotation.Single
import site.addzero.repro.feature.alpha.AlphaRepository

@Single
class BetaService(
    private val alphaRepository: AlphaRepository,
) {
    fun label(): String = "beta(${alphaRepository.label()})"
}
