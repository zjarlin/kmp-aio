package site.addzero.repro.feature.alpha

import org.koin.core.annotation.Single

@Single
class AlphaRepository {
    fun label(): String = "alpha"
}
