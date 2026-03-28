package site.addzero.demo.gamma

import org.koin.core.annotation.Single

@Single
class DemoGammaService {
    fun marker(): String = "gamma"
}
