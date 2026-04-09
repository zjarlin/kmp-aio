package site.addzero.kcloud

import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import org.koin.core.annotation.Single
import site.addzero.kcloud.shell.overlay.GlobalUiNotificationCenter

private val desktopGlobalExceptionHandlerInstalled = AtomicBoolean(false)

@Single(createdAtStart = true)
class DesktopGlobalExceptionHandlerInstaller {
    init {
        installDesktopGlobalExceptionHandler()
    }
}

private fun installDesktopGlobalExceptionHandler() {
    if (!desktopGlobalExceptionHandlerInstalled.compareAndSet(false, true)) {
        return
    }
    val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        if (throwable is CancellationException) {
            previousHandler?.uncaughtException(thread, throwable)
            return@setDefaultUncaughtExceptionHandler
        }
        System.err.println("KCloud 运行时异常 [${thread.name}]")
        System.err.println("根因: ${throwable.rootCauseDescription()}")
        GlobalUiNotificationCenter.showError(
            title = "运行异常",
            message = throwable.toastMessage(),
        )
        if (previousHandler != null) {
            previousHandler.uncaughtException(thread, throwable)
        } else {
            throwable.printStackTrace(System.err)
        }
    }
}

fun printDesktopStartupFailure(throwable: Throwable) {
    System.err.println("KCloud 桌面启动失败。")
    System.err.println("根因: ${throwable.rootCauseDescription()}")
    throwable.printStackTrace(System.err)
}

private fun Throwable.toastMessage(): String {
    val rootCause = rootCause()
    val message = rootCause.message?.trim().orEmpty()
    if (message.isNotBlank()) {
        return message
    }
    return rootCause::class.simpleName ?: "未知异常"
}

private fun Throwable.rootCauseDescription(): String {
    val rootCause = rootCause()
    val message = rootCause.message?.trim().orEmpty()
    if (message.isNotBlank()) {
        return "${rootCause::class.qualifiedName}: $message"
    }
    return rootCause::class.qualifiedName ?: "未知异常"
}

private fun Throwable.rootCause(): Throwable {
    var current = this
    while (current.cause != null) {
        current = current.cause!!
    }
    return current
}
