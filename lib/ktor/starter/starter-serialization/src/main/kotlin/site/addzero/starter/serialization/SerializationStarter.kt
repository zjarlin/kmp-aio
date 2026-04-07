package site.addzero.starter.serialization

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.starter.KtorAppStarter

@Module
@ComponentScan("site.addzero.starter.serialization")
class SerializationStarterKoinModule

@Single
class SerializationStarter : KtorAppStarter {
    override val enable: Boolean
        get() = true

    override fun Application.onInstall() {
        install(ContentNegotiation) {
            json(site.addzero.core.network.json.json)
        }
    }
}
