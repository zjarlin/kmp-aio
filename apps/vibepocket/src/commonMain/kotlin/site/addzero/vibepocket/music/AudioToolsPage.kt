package site.addzero.vibepocket.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.addzero.component.glass.GlassTheme
import site.addzero.component.glass.NeonGlassCard
import site.addzero.ioc.annotation.Bean

/**
 * 音频工具入口页面
 *
 * 以 GlassCard 卡片列表展示各种音频工具功能入口，
 * 点击卡片后打开对应的 Dialog。
 */
@Composable
@Bean(tags = ["screen"])
fun AudioToolsPage() {
    var showUploadCoverDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "🛠️ 音频工具",
                color = GlassTheme.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "选择一个工具开始操作",
                color = GlassTheme.TextTertiary,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ── 工具卡片列表 ──
            ToolEntry(
                icon = "🎤",
                title = "翻唱上传",
                description = "上传音频 URL，使用 AI 进行翻唱，支持选择模型版本和声线性别",
                onClick = { showUploadCoverDialog = true },
            )
        }
    }

    // ── Dialogs ──
    if (showUploadCoverDialog) {
        UploadCoverFormDialog(onDismiss = { showUploadCoverDialog = false })
    }
}

/**
 * 单个工具入口卡片
 */
@Composable
private fun ToolEntry(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    NeonGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        glowColor = GlassTheme.NeonCyan,
        intensity = 0.4f,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = icon, fontSize = 36.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = GlassTheme.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = GlassTheme.TextTertiary,
                    fontSize = 13.sp,
                )
            }
            Text(
                text = "→",
                color = GlassTheme.NeonCyan,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
