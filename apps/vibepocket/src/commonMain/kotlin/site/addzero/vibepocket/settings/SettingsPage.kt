package site.addzero.vibepocket.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.ioc.annotation.Bean
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.model.ConfigEntry
import site.addzero.vibepocket.ui.StudioEmptyState
import site.addzero.vibepocket.ui.StudioPill
import site.addzero.vibepocket.ui.StudioSectionCard
import site.addzero.vibepocket.ui.SunoTokenApplyHint

@Composable
@Bean(tags = ["screen"])
fun SettingsPage() {
    val scope = rememberCoroutineScope()
    var sunoToken by remember { mutableStateOf("") }
    var sunoBaseUrl by remember { mutableStateOf("https://api.sunoapi.org/api/v1") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sunoToken = ServerApiClient.getConfig("suno_api_token") ?: ""
        sunoBaseUrl = ServerApiClient.getConfig("suno_api_base_url") ?: "https://api.sunoapi.org/api/v1"
        loaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (!loaded) {
            StudioEmptyState(
                icon = "⏳",
                title = "读取配置中",
                description = "正在从本地 server 拉取你的音乐模块配置。",
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            MusicConfigEditor(
                sunoToken = sunoToken,
                onTokenChange = { sunoToken = it },
                sunoBaseUrl = sunoBaseUrl,
                onBaseUrlChange = { sunoBaseUrl = it },
                onSave = {
                    scope.launch {
                        ServerApiClient.configApi.updateConfig(
                            ConfigEntry("suno_api_token", sunoToken, "Suno API Token"),
                        )
                        ServerApiClient.configApi.updateConfig(
                            ConfigEntry("suno_api_base_url", sunoBaseUrl, "Suno API Base URL"),
                        )
                    }
                },
            )
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
    StudioSectionCard(
        modifier = Modifier.fillMaxWidth(),
        title = "音乐接口配置",
        subtitle = "这里只保留音乐模块配置。界面已经切回清晰的蓝色 Material 3 风格。",
        action = {
            StudioPill(
                text = "Music service",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        },
    ) {
        SunoTokenApplyHint(
            intro = "如果你还没申请过 Suno API Token，这里可以直接跳去控制台申请。",
            introStyle = MaterialTheme.typography.bodyMedium,
            introColor = MaterialTheme.colorScheme.onSurfaceVariant,
            linkStyle = MaterialTheme.typography.bodyMedium,
            linkColor = MaterialTheme.colorScheme.primary,
        )

        OutlinedTextField(
            value = sunoToken,
            onValueChange = onTokenChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Suno API Token") },
            placeholder = { Text("sk-...") },
            singleLine = true,
        )
        OutlinedTextField(
            value = sunoBaseUrl,
            onValueChange = onBaseUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Suno API Base URL") },
            placeholder = { Text("https://api.sunoapi.org/api/v1") },
            singleLine = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
            ) {
                Text("保存配置")
            }
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f),
            ) {
                Text("保留本地值")
            }
        }
    }
}
