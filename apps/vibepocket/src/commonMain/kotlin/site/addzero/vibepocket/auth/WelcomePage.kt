package site.addzero.vibepocket.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.vibepocket.ui.*

@Composable
fun WelcomePage(
    onEnter: (sunoToken: String, sunoBaseUrl: String) -> Unit,
) {
    var token by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("https://api.sunoapi.org/api/v1") }
    var step by remember { mutableStateOf(0) }

    VibeGlassBackdrop {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            WelcomeActionPanel(
                step = step,
                token = token,
                onTokenChange = { token = it },
                baseUrl = baseUrl,
                onBaseUrlChange = { baseUrl = it },
                onAdvance = { step = 1 },
                onSubmit = { onEnter(token, baseUrl) },
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            )
        }
    }
}

@Composable
private fun WelcomeActionPanel(
    step: Int,
    token: String,
    onTokenChange: (String) -> Unit,
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    onAdvance: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        AnimatedContent(targetState = step) { currentStep ->
            when (currentStep) {
                0 -> WelcomeIntroCard(onAdvance = onAdvance)
                else -> ApiConfigCard(
                    token = token,
                    onTokenChange = onTokenChange,
                    baseUrl = baseUrl,
                    onBaseUrlChange = onBaseUrlChange,
                    onSubmit = onSubmit,
                )
            }
        }
    }
}

@Composable
private fun WelcomeIntroCard(onAdvance: () -> Unit) {
    val palette = VibeGlassTheme.palette
    VibeGlassPanel {
        VibeGlassTag(text = "Step 1 / 2", accent = palette.glacier)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "先把工作台点亮",
            style = MaterialTheme.typography.headlineLarge,
            color = palette.ink,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "我们先接上 Suno 接口，再把整个音乐链路从“能用”继续打磨成“顺手”。",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.inkSoft,
        )
        Spacer(modifier = Modifier.height(22.dp))
        VibeGlassButton(
            text = "开始配置",
            onClick = onAdvance,
            modifier = Modifier.fillMaxWidth(),
            leading = "→",
        )
    }
}

@Composable
private fun ApiConfigCard(
    token: String,
    onTokenChange: (String) -> Unit,
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val palette = VibeGlassTheme.palette
    VibeGlassPanel {
        VibeGlassTag(text = "Step 2 / 2", accent = palette.aqua)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "接上 Suno API",
            style = MaterialTheme.typography.headlineLarge,
            color = palette.ink,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "你可以先填 token 和 base URL，后面在设置页里继续调整。暂时没 token 也能先进工作台看新界面。",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.inkSoft,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SunoTokenApplyHint(
            intro = "没申请过的话，先去 Suno 控制台申请 Token，再回来填这里。",
            introStyle = MaterialTheme.typography.bodyMedium,
            introColor = palette.inkSoft,
            linkStyle = MaterialTheme.typography.bodyMedium,
            linkColor = palette.aqua,
        )
        Spacer(modifier = Modifier.height(22.dp))
        VibeGlassTextField(
            value = token,
            onValueChange = onTokenChange,
            label = "Suno API Token",
            placeholder = "sk-...",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(14.dp))
        VibeGlassTextField(
            value = baseUrl,
            onValueChange = onBaseUrlChange,
            label = "API Base URL",
            placeholder = "https://api.sunoapi.org/api/v1",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VibeGlassButton(
                text = "进入工作台",
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                leading = "↗",
            )
            VibeGlassButton(
                text = "稍后再配",
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                style = VibeGlassButtonStyle.Secondary,
            )
        }
    }
}
