package site.addzero.coding.playground.demo.alpha

import org.koin.core.annotation.Single

@Single
class DemoAlphaService {
    fun marker(): String = "alpha"
}
