package site.addzero.starter.koin

import io.ktor.server.application.*
import io.ktor.util.AttributeKey
import org.koin.core.KoinApplication
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.KoinApplicationStarted
import org.koin.logger.slf4jLogger
import site.addzero.starter.AppStarter
import site.addzero.starter.AppStarterTest


/**
 * Koin 初始化入口。
 *
 * 因为所有其他 Starter 的发现依赖 Koin，所以 Koin 必须最先初始化，
 * 不走 AppStarter 自动发现流程，而是由 Application 入口手动调用。
 */
fun Application.installKoin(configure: KoinApplication.() -> Unit = {}) {
    install(Koin) {
        slf4jLogger()
        configure()
    }
}

