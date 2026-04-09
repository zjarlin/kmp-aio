package site.addzero.kcloud.shell.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * @author zjarlin
 * @date 2026/04/09
 * @constructor 创建[GlobalUiNotification]
 * @param [title]
 * @param [message]
 * @param [duration]
 * @param [withDismissAction]
 */
data class GlobalUiNotification(
    val title: String,
    val message: String,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val withDismissAction: Boolean = false,
)

object GlobalUiNotificationCenter {
    private val _notifications = MutableSharedFlow<GlobalUiNotification>(extraBufferCapacity = 16)
    val notifications = _notifications.asSharedFlow()

    fun show(notification: GlobalUiNotification) {
        _notifications.tryEmit(notification)
    }

    fun showError(title: String, message: String) {
        show(
            notification =
                GlobalUiNotification(
                    title = title,
                    message = message,
                    duration = SnackbarDuration.Long,
                    withDismissAction = true,
                ),
        )
    }
}

@Composable
fun RenderGlobalNotificationOverlay() {
    val hostState = remember { SnackbarHostState() }

    LaunchedEffect(hostState) {
        GlobalUiNotificationCenter.notifications.collect { notification ->
            val message = notification.toSnackbarMessage()
            if (message.isBlank()) {
                return@collect
            }
            hostState.showSnackbar(
                message = message,
                withDismissAction = notification.withDismissAction,
                duration = notification.duration,
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        SnackbarHost(
            hostState = hostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
        )
    }
}

private fun GlobalUiNotification.toSnackbarMessage(): String {
    val normalizedTitle = title.trim()
    val normalizedMessage = message.trim()
    if (normalizedTitle.isBlank()) {
        return normalizedMessage
    }
    if (normalizedMessage.isBlank()) {
        return normalizedTitle
    }
    if (normalizedTitle == normalizedMessage) {
        return normalizedTitle
    }
    return "$normalizedTitle\n$normalizedMessage"
}
