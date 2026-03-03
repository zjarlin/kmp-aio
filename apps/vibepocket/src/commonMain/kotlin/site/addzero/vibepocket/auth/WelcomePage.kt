package site.addzero.vibepocket.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.addzero.component.glass.*
import site.addzero.ioc.annotation.Bean

/**
 * 欢迎页 — 引导用户输入 Suno API Key 后进入主界面。
 *
 * 全屏玻璃风格，不显示侧边栏。
 */
@Composable
@Bean(tags = ["screen"])
fun WelcomePage(
    onEnter: (sunoToken: String, sunoBaseUrl: String) -> Unit,
) {
    var token by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("https://api.sunoapi.org/api/v1") }
    var step by remember { mutableStateOf(0) } // 0=欢迎, 1=输入

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassTheme.DarkBackground),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(targetState = step) { currentStep ->
            when (currentStep) {
                0 -> WelcomeHero(onGetStarted = { step = 1 })
                1 -> ApiKeyForm(
                    token = token,
                    onTokenChange = { token = it },
                    baseUrl = baseUrl,
                    onBaseUrlChange = { baseUrl = it },
                    onSubmit = { onEnter(token, baseUrl) },
                )
            }
        }
    }
}

@Composable
private fun WelcomeHero(onGetStarted: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(48.dp),
    ) {
        Text(text = "🎵", fontSize = 72.sp)
        Text(
            text = "Vibepocket",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "AI 音乐创作工作台",
            color = GlassTheme.NeonCyan,
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "搜索歌词灵感、用 Suno AI 生成原创音乐\n一站式 Vibe Coding 体验",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        NeonGlassButton(
            text = "开始使用 →",
            onClick = onGetStarted,
            glowColor = GlassTheme.NeonPurple,
        )
    }
}

@Composable
private fun ApiKeyForm(
    token: String,
    onTokenChange: (String) -> Unit,
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .widthIn(max = 480.dp)
            .padding(32.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "🔑 配置 API",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "输入你的 Suno API Token 即可开始创作。\n没有？去 sunoapi.org 注册一个。",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                lineHeight = 20.sp,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Suno API Token", color = GlassTheme.TextSecondary, fontSize = 12.sp)
                GlassTextField(
                    value = token,
                    onValueChange = onTokenChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "sk-...",
                    shape = RoundedCornerShape(10.dp),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("API Base URL", color = GlassTheme.TextSecondary, fontSize = 12.sp)
                GlassTextField(
                    value = baseUrl,
                    onValueChange = onBaseUrlChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "https://api.sunoapi.org/api/v1",
                    shape = RoundedCornerShape(10.dp),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NeonGlassButton(
                    text = "🚀 进入工作台",
                    onClick = onSubmit,
                    glowColor = GlassTheme.NeonCyan,
                    enabled = token.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
                GlassButton(
                    text = "跳过",
                    onClick = { onSubmit() },
                )
            }
        }
    }
}
