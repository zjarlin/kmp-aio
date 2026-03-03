package site.addzero.vibepocket.settings

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import site.addzero.component.glass.GlassCard
import site.addzero.component.glass.GlassTextField
import site.addzero.component.glass.GlassTheme
import site.addzero.component.glass.NeonGlassButton
import site.addzero.ioc.annotation.Bean
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.model.ConfigEntry

private enum class SettingsTab(val title: String, val icon: String) {
    MUSIC("音乐", "🎵"),
    IMAGE("图片", "🖼️"),
    VIDEO("视频", "🎬"),
}

/**
 * 设置页面 — 从内嵌 server DB 读写配置，不再依赖 ConfigStore。
 */
@Composable
@Bean(tags = ["screen"])
fun SettingsPage() {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(SettingsTab.MUSIC) }

    // 音乐模块配置
    var sunoToken by remember { mutableStateOf("") }
    var sunoBaseUrl by remember { mutableStateOf("https://api.sunoapi.org/api/v1") }
    var loaded by remember { mutableStateOf(false) }

    // 启动时从 server 加载
    LaunchedEffect(Unit) {
        sunoToken = ServerApiClient.getConfig("suno_api_token") ?: ""
        sunoBaseUrl = ServerApiClient.getConfig("suno_api_base_url") ?: "https://api.sunoapi.org/api/v1"
        loaded = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "⚙️ 设置",
                color = GlassTheme.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(20.dp))
            SettingsTabBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    SettingsTab.MUSIC -> {
                        if (!loaded) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("加载中...", color = GlassTheme.TextSecondary)
                            }
                        } else {
                            MusicConfigEditor(
                                sunoToken = sunoToken,
                                onTokenChange = { sunoToken = it },
                                sunoBaseUrl = sunoBaseUrl,
                                onBaseUrlChange = { sunoBaseUrl = it },
                                onSave = {
                                    scope.launch {
                                        ServerApiClient.configApi.updateConfig(
                                            ConfigEntry("suno_api_token", sunoToken, "Suno API Token")
                                        )
                                        ServerApiClient.configApi.updateConfig(
                                            ConfigEntry("suno_api_base_url", sunoBaseUrl, "Suno API Base URL")
                                        )
                                    }
                                },
                            )
                        }
                    }
                    SettingsTab.IMAGE -> PlaceholderTab("🖼️", "图片模块", "AI 图片生成配置即将开放，敬请期待。")
                    SettingsTab.VIDEO -> PlaceholderTab("🎬", "视频模块", "视频生成 API 配置即将开放，敬请期待。")
                }
            }
        }
    }
}

@Composable
private fun SettingsTabBar(selectedTab: SettingsTab, onTabSelected: (SettingsTab) -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SettingsTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .then(if (isSelected) Modifier.background(GlassTheme.NeonCyan.copy(alpha = 0.15f)) else Modifier)
                        .clickable(interactionSource = interactionSource, indication = null) { onTabSelected(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${tab.icon} ${tab.title}",
                        color = if (isSelected) GlassTheme.NeonCyan else GlassTheme.TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicConfigEditor(
    sunoToken: String,
    onTokenChange: (String) -> Unit,
    sunoBaseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ConfigField(label = "Suno API Token", value = sunoToken, onValueChange = onTokenChange, placeholder = "sk-...")
        ConfigField(label = "Suno API Base URL", value = sunoBaseUrl, onValueChange = onBaseUrlChange, placeholder = "https://api.sunoapi.org/api/v1")
        Spacer(modifier = Modifier.height(8.dp))
        NeonGlassButton(
            text = "💾 保存配置",
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            glowColor = GlassTheme.NeonCyan,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ConfigField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(label, color = GlassTheme.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            GlassTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = placeholder,
                shape = RoundedCornerShape(8.dp),
            )
        }
    }
}

@Composable
private fun PlaceholderTab(icon: String, title: String, message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GlassCard(modifier = Modifier.widthIn(max = 400.dp).padding(32.dp), shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(icon, fontSize = 48.sp)
                Text(title, color = GlassTheme.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(message, color = GlassTheme.TextTertiary, fontSize = 14.sp)
            }
        }
    }
}
