package site.addzero.remotecompose.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.remotecompose.shared.RemoteComposeAction
import site.addzero.remotecompose.shared.RemoteComposeActionButton
import site.addzero.remotecompose.shared.RemoteComposeActionRowNode
import site.addzero.remotecompose.shared.RemoteComposeBulletListNode
import site.addzero.remotecompose.shared.RemoteComposeButtonStyle
import site.addzero.remotecompose.shared.RemoteComposeCardNode
import site.addzero.remotecompose.shared.RemoteComposeColumnNode
import site.addzero.remotecompose.shared.RemoteComposeNode
import site.addzero.remotecompose.shared.RemoteComposeRowNode
import site.addzero.remotecompose.shared.RemoteComposeScreenPayload
import site.addzero.remotecompose.shared.RemoteComposeStatItem
import site.addzero.remotecompose.shared.RemoteComposeStatsNode
import site.addzero.remotecompose.shared.RemoteComposeTextNode
import site.addzero.remotecompose.shared.RemoteComposeTextStyle

@Composable
fun RemoteComposeRenderer(
    screen: RemoteComposeScreenPayload,
    onAction: suspend (RemoteComposeAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        RenderRemoteComposeNode(
            node = screen.root,
            onAction = onAction,
        )
    }
}

@Composable
private fun RenderRemoteComposeNode(
    node: RemoteComposeNode,
    onAction: suspend (RemoteComposeAction) -> Unit,
) {
    when (node) {
        is RemoteComposeActionRowNode -> RemoteActionRow(
            node = node,
            onAction = onAction,
        )
        is RemoteComposeBulletListNode -> RemoteBulletList(node = node)
        is RemoteComposeCardNode -> RemoteCardNodeView(
            node = node,
            onAction = onAction,
        )
        is RemoteComposeColumnNode -> Column(
            verticalArrangement = Arrangement.spacedBy(node.gap.dp),
        ) {
            node.children.forEach { child ->
                RenderRemoteComposeNode(child, onAction)
            }
        }
        is RemoteComposeRowNode -> Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(node.gap.dp),
        ) {
            node.children.forEach { child ->
                Box(
                    modifier = Modifier.weight(1f, fill = true),
                ) {
                    RenderRemoteComposeNode(child, onAction)
                }
            }
        }
        is RemoteComposeStatsNode -> RemoteStatsNode(node = node)
        is RemoteComposeTextNode -> RemoteTextNodeView(node = node)
    }
}

@Composable
private fun RemoteCardNodeView(
    node: RemoteComposeCardNode,
    onAction: suspend (RemoteComposeAction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = toneColor(node.tone).copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = node.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (node.subtitle.isNotBlank()) {
                    Text(
                        text = node.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (node.actions.isNotEmpty()) {
                RemoteActionButtons(
                    buttons = node.actions,
                    onAction = onAction,
                )
            }

            node.children.forEach { child ->
                RenderRemoteComposeNode(
                    node = child,
                    onAction = onAction,
                )
            }
        }
    }
}

@Composable
private fun RemoteActionRow(
    node: RemoteComposeActionRowNode,
    onAction: suspend (RemoteComposeAction) -> Unit,
) {
    RemoteActionButtons(
        buttons = node.buttons,
        onAction = onAction,
    )
}

@Composable
private fun RemoteActionButtons(
    buttons: List<RemoteComposeActionButton>,
    onAction: suspend (RemoteComposeAction) -> Unit,
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        buttons.forEach { button ->
            when (button.style) {
                RemoteComposeButtonStyle.PRIMARY -> Button(
                    onClick = {
                        scope.launch {
                            onAction(button.action)
                        }
                    },
                ) {
                    Text(button.label)
                }

                RemoteComposeButtonStyle.SECONDARY,
                RemoteComposeButtonStyle.GHOST,
                -> OutlinedButton(
                    onClick = {
                        scope.launch {
                            onAction(button.action)
                        }
                    },
                ) {
                    Text(button.label)
                }
            }
        }
    }
}

@Composable
private fun RemoteStatsNode(
    node: RemoteComposeStatsNode,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        node.items.forEach { item ->
            RemoteStatCard(
                item = item,
                modifier = Modifier.weight(1f, fill = true),
            )
        }
    }
}

@Composable
private fun RemoteStatCard(
    item: RemoteComposeStatItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = item.value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = toneColor(item.tone),
        )
        if (item.caption.isNotBlank()) {
            Text(
                text = item.caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RemoteBulletList(
    node: RemoteComposeBulletListNode,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        node.items.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .width(8.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary),
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun RemoteTextNodeView(
    node: RemoteComposeTextNode,
) {
    val style = when (node.style) {
        RemoteComposeTextStyle.TITLE -> MaterialTheme.typography.titleLarge
        RemoteComposeTextStyle.SUBTITLE -> MaterialTheme.typography.titleMedium
        RemoteComposeTextStyle.BODY -> MaterialTheme.typography.bodyMedium
        RemoteComposeTextStyle.CAPTION -> MaterialTheme.typography.bodySmall
        RemoteComposeTextStyle.OVERLINE -> MaterialTheme.typography.labelSmall
        RemoteComposeTextStyle.MONO -> MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
        )
    }

    Text(
        text = node.text,
        style = style,
        color = when (node.style) {
            RemoteComposeTextStyle.CAPTION,
            RemoteComposeTextStyle.OVERLINE,
            -> MaterialTheme.colorScheme.onSurfaceVariant

            else -> toneColor(node.tone).takeIf { node.tone != site.addzero.remotecompose.shared.RemoteComposeTone.NEUTRAL }
                ?: MaterialTheme.colorScheme.onSurface
        },
    )
}
