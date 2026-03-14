package com.kcloud.plugins.quicktransfer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 同步阶段定义
 */
enum class SyncStage(
    val displayName: String,
    val icon: ImageVector,
    val description: String
) {
    SCANNING(
        displayName = "扫描文件",
        icon = Icons.Default.Search,
        description = "正在扫描本地和远程文件变化"
    ),
    ANALYZING(
        displayName = "分析差异",
        icon = Icons.Default.Analytics,
        description = "计算需要同步的文件列表"
    ),
    TRANSFERRING(
        displayName = "传输文件",
        icon = Icons.Default.CloudUpload,
        description = "正在上传/下载文件"
    ),
    VERIFYING(
        displayName = "验证数据",
        icon = Icons.Default.Verified,
        description = "校验文件完整性"
    ),
    FINALIZING(
        displayName = "完成同步",
        icon = Icons.Default.DoneAll,
        description = "更新同步状态"
    ),
    CONFLICTS(
        displayName = "处理冲突",
        icon = Icons.Default.Warning,
        description = "需要处理文件冲突"
    ),
    IDLE(
        displayName = "等待中",
        icon = Icons.Default.Schedule,
        description = "准备开始同步"
    )
}

/**
 * 阶段状态
 */
enum class StageStatus {
    PENDING,      // 等待执行
    IN_PROGRESS,  // 进行中
    COMPLETED,    // 已完成
    ERROR,        // 出错
    SKIPPED       // 已跳过
}

/**
 * 阶段进度数据
 */
data class StageProgress(
    val stage: SyncStage,
    val status: StageStatus,
    val progress: Float = 0f,  // 0.0 - 1.0
    val currentItem: String = "",
    val itemCount: Int = 0,
    val currentIndex: Int = 0,
    val speed: String = "",    // 如 "5.2 MB/s"
    val eta: String = ""       // 如 "2分钟"
)

/**
 * 分阶段进度指示器 - 显示同步的各个阶段
 *
 * @param stages 所有阶段及其进度
 * @param currentStage 当前活跃的阶段
 * @param modifier 修饰符
 */
@Composable
fun StagedProgressIndicator(
    stages: List<StageProgress>,
    currentStage: SyncStage?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 阶段步骤条
        StageStepBar(stages = stages, currentStage = currentStage)

        Spacer(modifier = Modifier.height(16.dp))

        // 当前阶段详细信息
        currentStage?.let { stage ->
            val stageProgress = stages.find { it.stage == stage }
            stageProgress?.let {
                CurrentStageDetail(progress = it)
            }
        }
    }
}

/**
 * 阶段步骤条 - 水平显示所有阶段
 */
@Composable
private fun StageStepBar(
    stages: List<StageProgress>,
    currentStage: SyncStage?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.filter { it.stage != SyncStage.IDLE }.forEachIndexed { index, stageProgress ->
            val stage = stageProgress.stage
            val isCurrent = stage == currentStage
            val isCompleted = stageProgress.status == StageStatus.COMPLETED
            val isError = stageProgress.status == StageStatus.ERROR

            // 阶段节点
            StageNode(
                stage = stage,
                isCurrent = isCurrent,
                isCompleted = isCompleted,
                isError = isError
            )

            // 连接线（除了最后一个）
            if (index < stages.size - 2) {  // -2 因为过滤了 IDLE
                val nextStage = stages.filter { it.stage != SyncStage.IDLE }.getOrNull(index + 1)
                val lineProgress = when {
                    isCompleted -> 1f
                    isCurrent -> stageProgress.progress
                    else -> 0f
                }

                ConnectorLine(
                    progress = lineProgress,
                    isCompleted = nextStage?.status == StageStatus.COMPLETED || isCompleted,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 阶段节点
 */
@Composable
private fun StageNode(
    stage: SyncStage,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isError -> MaterialTheme.colorScheme.error
        isCompleted -> MaterialTheme.colorScheme.primary
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconColor = when {
        isError -> MaterialTheme.colorScheme.onError
        isCompleted -> MaterialTheme.colorScheme.onPrimary
        isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val icon = when {
        isError -> Icons.Default.Error
        isCompleted -> Icons.Default.Check
        else -> stage.icon
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCurrent) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .scale(if (isCurrent) scale else 1f)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stage.displayName,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stage.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isCurrent || isCompleted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 连接线
 */
@Composable
private fun ConnectorLine(
    progress: Float,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(2.dp)
            .padding(horizontal = 4.dp)
    ) {
        // 背景线
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(1.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        // 进度线
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(1.dp))
                .background(
                    if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    }
                )
        )
    }
}

/**
 * 当前阶段详细信息
 */
@Composable
private fun CurrentStageDetail(
    progress: StageProgress,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 阶段标题和描述
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = progress.stage.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = progress.stage.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = progress.stage.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 进度条
            if (progress.status == StageStatus.IN_PROGRESS) {
                LinearProgressIndicator(
                    progress = { progress.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 进度信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(progress.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (progress.speed.isNotEmpty()) {
                        Text(
                            text = progress.speed,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (progress.eta.isNotEmpty()) {
                        Text(
                            text = "剩余 ${progress.eta}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 当前处理项
            if (progress.currentItem.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "正在处理: ${progress.currentItem}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 计数信息
            if (progress.itemCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${progress.currentIndex} / ${progress.itemCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 简洁的分阶段进度条（用于悬浮窗口）
 */
@Composable
fun CompactStagedProgress(
    currentStage: SyncStage,
    progress: Float,
    currentItem: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 阶段图标
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = currentStage.icon,
                contentDescription = currentStage.displayName,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 进度信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currentStage.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (currentItem.isNotEmpty()) {
                Text(
                    text = currentItem,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 百分比
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 预览用示例
 */
@Composable
fun StagedProgressIndicatorPreview() {
    val stages = listOf(
        StageProgress(SyncStage.SCANNING, StageStatus.COMPLETED, 1f),
        StageProgress(SyncStage.ANALYZING, StageStatus.COMPLETED, 1f),
        StageProgress(SyncStage.TRANSFERRING, StageStatus.IN_PROGRESS, 0.65f,
            currentItem = "document.pdf", itemCount = 100, currentIndex = 65,
            speed = "2.5 MB/s", eta = "30秒"),
        StageProgress(SyncStage.VERIFYING, StageStatus.PENDING),
        StageProgress(SyncStage.FINALIZING, StageStatus.PENDING)
    )

    MaterialTheme {
        Surface {
            StagedProgressIndicator(
                stages = stages,
                currentStage = SyncStage.TRANSFERRING
            )
        }
    }
}
