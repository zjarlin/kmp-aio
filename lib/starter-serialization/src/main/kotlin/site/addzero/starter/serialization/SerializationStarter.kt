package site.addzero.starter.serialization

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter

@Module
@Configuration("vibepocket")
@ComponentScan("site.addzero.starter.serialization")
class SerializationStarterKoinModule

@Named("serializationStarter")
@Single(binds = [AppStarter::class])
class SerializationStarter : AppStarter {

    override fun Application.onInstall() {
        install(ContentNegotiation) {
            json(site.addzero.core.network.json.json)
        }
    }
}
