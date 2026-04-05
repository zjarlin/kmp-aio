package site.addzero.starter.koin

import io.ktor.server.application.*
import io.ktor.util.AttributeKey
import org.koin.core.KoinApplication
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.KoinApplicationStarted
import org.koin.logger.slf4jLogger
import site.addzero.starter.AppStarter

private val startersExecutedKey = AttributeKey<Boolean>("site.addzero.starter.koin.startersExecuted")

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

/**
 * 执行所有已注册的 AppStarter。
 *
 * 从 Koin 容器中获取所有 AppStarter 实现，按 order 排序，
 * 过滤条件后依次执行 onInstall()。
 */
fun Application.runStarters() {
    fun execute(stage: String) {
        if (attributes.getOrNull(startersExecutedKey) == true) return
        val app = this
        val koin = app.getKoin()
        val starters = koin
            .getAll<AppStarter<Application>>()
            .filter { starter -> with(starter) { app.enable() } }
            .sortedBy { it.order }
        attributes.put(startersExecutedKey, true)
        log.info("Resolved ${starters.size} AppStarter(s) at stage=$stage")
        for (starter in starters) {
            log.info("Installing starter: ${starter::class.simpleName} (order=${starter.order})")
            with(starter) { app.onInstall() }
        }
    }
    execute(stage = "after-koin-install")
    if (attributes.getOrNull(startersExecutedKey) != true) {
        monitor.subscribe(KoinApplicationStarted) {
            execute(stage = "koin-started")
        }
    }
}
