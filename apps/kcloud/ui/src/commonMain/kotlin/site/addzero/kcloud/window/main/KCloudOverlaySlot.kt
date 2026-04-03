package site.addzero.kcloud.window.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import org.koin.core.annotation.Single
import site.addzero.kcloud.design.button.KCloudButtonVariant
import site.addzero.kcloud.design.button.KCloudIconButton
import site.addzero.kcloud.plugins.system.aichat.AiChatWorkbenchState
import site.addzero.kcloud.plugins.system.aichat.screen.AiChatPanel
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.window.spi.KCloudOverlaySlotSpi

@Single(
    binds = [
        KCloudOverlaySlotSpi::class,
    ],
)
class DefaultKCloudOverlaySlot(
    private val shellState: KCloudShellState,
    private val aiChatState: AiChatWorkbenchState,
) : KCloudOverlaySlotSpi {
    @Composable
    override fun Render() {
        KCloudAiAssistantDialog(
            visible = shellState.aiAssistantVisible,
            state = aiChatState,
            onDismiss = shellState::hideAiAssistant,
        )
    }
}

@Composable
private fun KCloudAiAssistantDialog(
    visible: Boolean,
    state: AiChatWorkbenchState,
    onDismiss: () -> Unit,
) {
    var rendered by remember { mutableStateOf(visible) }

    LaunchedEffect(visible) {
        if (visible) {
            rendered = true
        } else {
            delay(AI_ASSISTANT_DIALOG_ANIMATION_MS.toLong())
            rendered = false
        }
    }

    if (!rendered) {
        return
    }

    val transitionProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = AI_ASSISTANT_DIALOG_ANIMATION_MS),
        label = "ai-assistant-dialog",
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.36f * transitionProgress))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 1180.dp)
                    .heightIn(min = 680.dp, max = 920.dp)
                    .graphicsLayer {
                        alpha = transitionProgress
                        scaleX = 0.94f + (0.06f * transitionProgress)
                        scaleY = 0.96f + (0.04f * transitionProgress)
                        translationY = (1f - transitionProgress) * 28.dp.toPx()
                    }
                    .clip(RoundedCornerShape(32.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                tonalElevation = 14.dp,
                shadowElevation = 20.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Box(
                                    modifier = Modifier.padding(10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = "AI 助手",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "右上角快捷呼出，不占用主工作区导航。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        KCloudIconButton(
                            onClick = onDismiss,
                            tooltip = "关闭 AI 助手",
                            variant = KCloudButtonVariant.Outline,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                            )
                        }
                    }

                    AiChatPanel(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                    )
                }
            }
        }
    }
}

private const val AI_ASSISTANT_DIALOG_ANIMATION_MS = 180
