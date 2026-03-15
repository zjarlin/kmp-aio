package com.kcloud.plugins.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class Toast(
    val id: String,
    val message: String,
    val type: ToastType = ToastType.INFO,
)

enum class ToastType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
}

class ToastManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _toasts = mutableStateListOf<Toast>()
    val toasts: List<Toast> = _toasts

    fun show(
        message: String,
        type: ToastType = ToastType.INFO,
        durationMillis: Long = 3000,
    ) {
        val toast = Toast(
            id = Clock.System.now().toEpochMilliseconds().toString(),
            message = message,
            type = type,
        )
        _toasts.add(toast)

        scope.launch {
            delay(durationMillis)
            dismiss(toast.id)
        }
    }

    fun dismiss(id: String) {
        _toasts.removeAll { it.id == id }
    }
}

@Composable
fun ToastHost(
    toasts: List<Toast>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            toasts.forEach { toast ->
                ToastItem(toast = toast)
            }
        }
    }
}

@Composable
private fun ToastItem(toast: Toast) {
    val (containerColor, contentColor) = when (toast.type) {
        ToastType.INFO -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        ToastType.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        ToastType.WARNING -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        ToastType.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Text(
            text = toast.message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
