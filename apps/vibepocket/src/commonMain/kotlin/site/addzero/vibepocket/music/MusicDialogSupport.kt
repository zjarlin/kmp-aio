package site.addzero.vibepocket.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import site.addzero.media.playlist.player.DefaultPlaylistPlayer
import site.addzero.vibepocket.api.suno.SunoTaskDetail

@Composable
internal fun MusicActionDialog(
    title: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        confirmButton = {},
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        },
    )
}

@Composable
internal fun DialogHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun DialogStatusText(statusText: String?) {
    if (statusText.isNullOrBlank()) {
        return
    }
    Text(
        text = statusText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
internal fun DialogErrorText(
    errorMessage: String?,
    onClear: (() -> Unit)? = null,
) {
    if (errorMessage.isNullOrBlank()) {
        return
    }
    Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
    if (onClear != null) {
        OutlinedButton(onClick = onClear) {
            Text("清除错误")
        }
    }
}

@Composable
internal fun DialogSuccessTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
internal fun DialogInfoCard(
    title: String,
    body: String,
    accent: Color = MaterialTheme.colorScheme.tertiaryContainer,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = accent.copy(alpha = 0.55f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
internal fun DialogMonospaceValue(
    label: String,
    value: String,
) {
    DialogInfoCard(
        title = label,
        body = value,
        accent = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
internal fun DialogLinkCard(
    title: String,
    label: String,
    url: String,
) {
    val uriHandler = LocalUriHandler.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Button(
                onClick = {
                    uriHandler.openUri(url)
                },
            ) {
                Text(label)
            }
            Text(
                text = url,
                modifier = Modifier.clickable {
                    uriHandler.openUri(url)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Composable
internal fun DialogCloseButton(onDismiss: () -> Unit) {
    FilledTonalButton(onClick = onDismiss) {
        Text("关闭")
    }
}

@Composable
internal fun DialogTrackResults(
    detail: SunoTaskDetail,
    fallbackTaskId: String,
) {
    val tracks = detail.response?.sunoData ?: emptyList()
    if (tracks.isEmpty()) {
        DialogHint("任务完成了，但没有返回音轨结果。")
        return
    }

    DefaultPlaylistPlayer(
        items = tracks,
        modifier = Modifier.fillMaxWidth(),
        itemKey = { track ->
            track.playbackId(detail.taskId ?: fallbackTaskId)
        },
        titleOf = { track ->
            track.displayTitle()
        },
        subtitleOf = { track ->
            track.displaySubtitle((detail.taskId ?: fallbackTaskId).take(8))
        },
        durationMsOf = { track ->
            track.durationMsOrNull()
        },
        coverUrlOf = { track ->
            track.imageUrl
        },
        resolveAudioSource = { track ->
            track.resolvedAudioSource()
        },
    )
}
