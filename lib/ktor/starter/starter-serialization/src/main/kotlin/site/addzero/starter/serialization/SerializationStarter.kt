package site.addzero.starter.serialization

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter

@Module
@Configuration
@ComponentScan("site.addzero.starter.serialization")
class SerializationStarterKoinModule

@Single
class SerializationStarter : AppStarter {
    override val enable: Boolean
        get() = true

    override fun onInstall(application: Application) {
        application.install(ContentNegotiation) {
            json(site.addzero.core.network.json.json)
        }
    }
}
