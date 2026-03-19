package com.kcloud.features.quicktransfer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class DragState {
    NONE,
    DRAGGING,
    VALID,
    INVALID,
}

@Composable
fun DragDropUploadOverlay(
    dragState: DragState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(
                width = when (dragState) {
                    DragState.VALID -> 3.dp
                    DragState.INVALID -> 3.dp
                    else -> 1.dp
                },
                color = when (dragState) {
                    DragState.VALID -> MaterialTheme.colorScheme.primary
                    DragState.INVALID -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(12.dp),
            )
            .background(
                when (dragState) {
                    DragState.VALID -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    DragState.INVALID -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    else -> Color.Transparent
                },
            ),
    ) {
        AnimatedVisibility(
            visible = dragState != DragState.NONE,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = if (dragState == DragState.VALID) {
                            Icons.Default.CloudUpload
                        } else {
                            Icons.Default.Warning
                        },
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = if (dragState == DragState.VALID) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (dragState == DragState.VALID) {
                            "释放以上传文件"
                        } else {
                            "不支持的文件类型"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
fun UploadProgressBanner(
    visible: Boolean,
    uploadProgress: Float,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier,
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "正在处理文件...",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { uploadProgress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
