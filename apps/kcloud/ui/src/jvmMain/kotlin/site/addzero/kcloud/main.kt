@file:Suppress("SpellCheckingInspection")

package site.addzero.kcloud

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.server.engine.ApplicationEngine
import java.net.BindException
import site.addzero.kcloud.bootstrap.App
import site.addzero.kcloud.bootstrap.UiKoinBootstrapMode
import site.addzero.kcloud.di.initDesktopHostKoin
import site.addzero.kcloud.runtime.KCloudHostRuntime
import site.addzero.kcloud.server.startServer

private const val EMBEDDED_SERVER_DB_MODE_PROPERTY: String = "kcloud.server.db"
private const val EMBEDDED_SERVER_DB_MODE_SQLITE: String = "sqlite"

fun main() {
    try {
        configureEmbeddedServerDefaults()
        initDesktopHostKoin()
        val serverEngine = startServerOrReuseExisting()

        application {
            Window(
                onCloseRequest = {
                    serverEngine?.stop(
                        gracePeriodMillis = 1_000,
                        timeoutMillis = 2_000,
                    )
                    exitApplication()
                },
                title = "OKMY DICS",
            ) {
                App(
                    koinBootstrapMode = UiKoinBootstrapMode.AlreadyStarted,
                )
            }
        }
    } catch (throwable: Throwable) {
        printDesktopStartupFailure(throwable)
        throw throwable
    }
}

private fun configureEmbeddedServerDefaults() {
    if (System.getProperty(EMBEDDED_SERVER_DB_MODE_PROPERTY).isNullOrBlank()) {
        System.setProperty(EMBEDDED_SERVER_DB_MODE_PROPERTY, EMBEDDED_SERVER_DB_MODE_SQLITE)
    }
}

private fun startServerOrReuseExisting(): ApplicationEngine? {
    return try {
        startServer(wait = false)
    } catch (throwable: Throwable) {
        if (throwable.hasCause<BindException>()) {
            println(
                "Embedded server port ${KCloudHostRuntime.DEFAULT_SERVER_PORT} is already in use; " +
                    "reusing the existing backend instance.",
            )
            null
        } else {
            throw throwable
        }
    }
}

private inline fun <reified T : Throwable> Throwable.hasCause(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (current is T) {
            return true
        }
        current = current.cause
    }
    return false
}
