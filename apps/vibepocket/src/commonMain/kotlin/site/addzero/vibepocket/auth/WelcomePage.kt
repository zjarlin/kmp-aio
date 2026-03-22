package site.addzero.vibepocket.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import site.addzero.liquidglass.LiquidGlassButton
import site.addzero.liquidglass.LiquidGlassButtonStyle
import site.addzero.liquidglass.LiquidGlassWorkbenchDefaults
import site.addzero.liquidglass.liquidGlassSurface
import site.addzero.vibepocket.ui.StudioPill
import site.addzero.vibepocket.ui.StudioSectionCard
import site.addzero.vibepocket.ui.SunoTokenApplyHint

@Composable
fun WelcomePage(
    onEnter: (sunoToken: String, sunoBaseUrl: String) -> Unit,
) {
    var token by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("https://api.sunoapi.org/api/v1") }
    var step by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier.welcomeViewportFrame(),
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
                .welcomeCardFrame()
                .verticalScroll(scrollState),
        )
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
    StudioSectionCard(
        title = "先把工作台点亮",
        subtitle = "先把 Suno 连接接好，后面就可以在工作台里继续打磨音乐生产链路。",
        action = {
            StudioPill(
                text = "Step 1 / 2",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "当前这一步只做首次引导，不会把你锁死在欢迎页；即使暂时没有 Token，也可以先进入工作台看整体壳层和页面结构。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LiquidGlassButton(
                text = "开始配置",
                style = LiquidGlassButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = onAdvance,
            )
        }
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
    StudioSectionCard(
        title = "接上 Suno API",
        subtitle = "Token 和 Base URL 后面都能在设置页继续改；先填最小配置，把工作台跑起来。",
        action = {
            StudioPill(
                text = "Step 2 / 2",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
    ) {
        SunoTokenApplyHint(
            intro = "没申请过的话，先去 Suno 控制台申请 Token，再回来填这里。",
            introStyle = MaterialTheme.typography.bodyMedium,
            introColor = MaterialTheme.colorScheme.onSurfaceVariant,
            linkStyle = MaterialTheme.typography.bodyMedium,
            linkColor = MaterialTheme.colorScheme.primary,
        )
        WelcomeGlassTextField(
            value = token,
            onValueChange = onTokenChange,
            label = "Suno API Token",
            placeholder = "sk-...",
        )
        WelcomeGlassTextField(
            value = baseUrl,
            onValueChange = onBaseUrlChange,
            label = "API Base URL",
            placeholder = "https://api.sunoapi.org/api/v1",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiquidGlassButton(
                text = "进入工作台",
                style = LiquidGlassButtonStyle.Primary,
                onClick = onSubmit,
                modifier = welcomeActionButtonFrame(),
            )
            LiquidGlassButton(
                text = "稍后再配",
                onClick = onSubmit,
                modifier = welcomeActionButtonFrame(),
            )
        }
    }
}

@Composable
private fun WelcomeGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.welcomeInputSurface(),
            singleLine = true,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                )
            },
            shape = RoundedCornerShape(18.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

/** 欢迎页视口：四周留出呼吸边距，让引导卡片在桌面里更像独立舞台。 */
private fun Modifier.welcomeViewportFrame(): Modifier {
    return fillMaxSize().padding(24.dp)
}

/** 欢迎卡片宽度：限制最大宽度，避免文案在大桌面里拉得过散。 */
private fun Modifier.welcomeCardFrame(): Modifier {
    return widthIn(max = 680.dp).fillMaxWidth()
}

/** 输入框玻璃底座：把表单控件压进统一材质，而不是默认纯色输入框。 */
private fun Modifier.welcomeInputSurface(): Modifier {
    return fillMaxWidth()
        .liquidGlassSurface(LiquidGlassWorkbenchDefaults.section)
        .background(Color.Transparent, RoundedCornerShape(18.dp))
}

/** 欢迎页操作按钮：两颗按钮等宽排布，保持首屏动作节奏稳定。 */
private fun RowScope.welcomeActionButtonFrame(): Modifier {
    return Modifier.weight(1f)
}
