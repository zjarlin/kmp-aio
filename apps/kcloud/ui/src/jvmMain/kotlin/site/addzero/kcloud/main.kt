@file:Suppress("SpellCheckingInspection")

package site.addzero.kcloud

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.server.engine.ApplicationEngine
import java.io.PrintWriter
import java.io.StringWriter
import java.net.BindException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import site.addzero.kcloud.bootstrap.App
import site.addzero.kcloud.bootstrap.UiKoinBootstrapMode
import site.addzero.kcloud.di.initDesktopHostKoin
import site.addzero.kcloud.runtime.KCloudHostRuntime
import site.addzero.kcloud.server.startServer

fun main() {
    runDesktopHostWithDiagnostics {
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
    }
}

private inline fun runDesktopHostWithDiagnostics(block: () -> Unit) {
    try {
        block()
    } catch (throwable: Throwable) {
        val startupFailureLog = persistStartupFailure(throwable)
        val rootCause = throwable.rootCause()
        System.err.println("KCloud 桌面启动失败。")
        System.err.println("根因: ${rootCause::class.qualifiedName}: ${rootCause.message ?: "(no message)"}")
        startupFailureLog?.let { path ->
            System.err.println("详细堆栈已写入: $path")
        }
        throwable.printStackTrace(System.err)
        throw throwable
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

private fun persistStartupFailure(throwable: Throwable): Path? {
    return runCatching {
        val logPath =
            Paths
                .get(System.getProperty("user.home"), ".kcloud", "logs", "desktop-startup-failure.log")
                .toAbsolutePath()
                .normalize()
        Files.createDirectories(logPath.parent)
        Files.writeString(logPath, throwable.stackTraceText())
        logPath
    }.getOrNull()
}

private fun Throwable.rootCause(): Throwable {
    var current = this
    while (current.cause != null) {
        current = current.cause!!
    }
    return current
}

private fun Throwable.stackTraceText(): String {
    val buffer = StringWriter()
    PrintWriter(buffer).use { writer ->
        printStackTrace(writer)
    }
    return buffer.toString()
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
