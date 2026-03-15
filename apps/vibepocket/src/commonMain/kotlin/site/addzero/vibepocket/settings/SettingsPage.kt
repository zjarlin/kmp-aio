package site.addzero.vibepocket.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import site.addzero.vibepocket.model.ConfigRuntimeInfo
import site.addzero.vibepocket.music.SunoWorkflowService
import site.addzero.vibepocket.platform.DirectoryLauncher
import site.addzero.vibepocket.ui.StudioEmptyState
import site.addzero.vibepocket.ui.StudioPill
import site.addzero.vibepocket.ui.StudioSectionCard
import site.addzero.vibepocket.ui.SunoTokenApplyHint

@Composable
@Bean(tags = ["screen"])
fun SettingsPage() {
    val scope = rememberCoroutineScope()
    var sunoToken by remember { mutableStateOf("") }
    var sunoBaseUrl by remember {
        mutableStateOf(site.addzero.vibepocket.api.suno.SunoApiClient.DEFAULT_BASE_URL)
    }
    var loaded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var runtimeInfo by remember { mutableStateOf<ConfigRuntimeInfo?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var feedbackIsError by remember { mutableStateOf(false) }

    suspend fun reloadFromServer() {
        val runtimeConfig = SunoWorkflowService.loadConfig()
        sunoToken = runtimeConfig.apiToken
        sunoBaseUrl = runtimeConfig.baseUrl
        runtimeInfo = runCatching { ServerApiClient.configApi.getRuntimeInfo() }.getOrNull()
        loaded = true
    }

    LaunchedEffect(Unit) {
        reloadFromServer()
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
                isSaving = isSaving,
                feedbackMessage = feedbackMessage,
                feedbackIsError = feedbackIsError,
                onSave = {
                    scope.launch {
                        isSaving = true
                        try {
                            SunoWorkflowService.saveConfig(
                                apiToken = sunoToken,
                                baseUrl = sunoBaseUrl,
                            )
                            reloadFromServer()
                            feedbackIsError = false
                            feedbackMessage = when (runtimeInfo?.storage) {
                                "sqlite" -> "已保存到本地 SQLite。"
                                else -> "配置已保存。"
                            }
                        } catch (error: Throwable) {
                            feedbackIsError = true
                            feedbackMessage = error.message?.takeIf { it.isNotBlank() } ?: "保存配置失败"
                        } finally {
                            isSaving = false
                        }
                    }
                },
                onReload = {
                    scope.launch {
                        reloadFromServer()
                        feedbackIsError = false
                        feedbackMessage = "已重新读取当前本地配置。"
                    }
                },
            )
            LocalStorageCard(
                runtimeInfo = runtimeInfo,
                onOpenCacheDir = {
                    val cacheDir = runtimeInfo?.cacheDir
                    if (cacheDir.isNullOrBlank()) {
                        feedbackIsError = true
                        feedbackMessage = "当前没有可打开的缓存目录。"
                    } else {
                        val opened = DirectoryLauncher.openDirectory(cacheDir)
                        feedbackIsError = !opened
                        feedbackMessage = if (opened) {
                            "已打开缓存目录。"
                        } else {
                            "打开缓存目录失败：$cacheDir"
                        }
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
    isSaving: Boolean,
    feedbackMessage: String?,
    feedbackIsError: Boolean,
    onSave: () -> Unit,
    onReload: () -> Unit,
) {
    StudioSectionCard(
        modifier = Modifier.fillMaxWidth(),
        title = "音乐接口配置",
        subtitle = "设置页现在直接读写本地 server 的配置表，并显示实际保存结果。",
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
        if (feedbackMessage != null) {
            Text(
                text = feedbackMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (feedbackIsError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
            ) {
                if (isSaving) {
                    Box {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                } else {
                    Text("保存配置")
                }
            }
            OutlinedButton(
                onClick = onReload,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
            ) {
                Text("重新读取")
            }
        }
    }
}

@Composable
private fun LocalStorageCard(
    runtimeInfo: ConfigRuntimeInfo?,
    onOpenCacheDir: () -> Unit,
) {
    StudioSectionCard(
        modifier = Modifier.fillMaxWidth(),
        title = "本地存储",
        subtitle = "这里展示当前 desktop 内嵌 server 实际使用的配置存储位置。",
        action = {
            StudioPill(
                text = runtimeInfo?.storage?.uppercase().orEmpty().ifBlank { "UNKNOWN" },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
    ) {
        RuntimeInfoLine(
            label = "当前配置库",
            value = when (runtimeInfo?.storage) {
                "sqlite" -> "SQLite"
                "postgres" -> "PostgreSQL"
                else -> "未知"
            },
        )
        RuntimeInfoLine(
            label = "SQLite 文件",
            value = runtimeInfo?.sqlitePath ?: "未提供",
        )
        RuntimeInfoLine(
            label = "数据目录",
            value = runtimeInfo?.dataDir ?: "未提供",
        )
        RuntimeInfoLine(
            label = "缓存目录",
            value = runtimeInfo?.cacheDir ?: "未提供",
        )
        OutlinedButton(
            onClick = onOpenCacheDir,
            enabled = !runtimeInfo?.cacheDir.isNullOrBlank(),
        ) {
            Text("打开缓存目录")
        }
    }
}

@Composable
private fun RuntimeInfoLine(
    label: String,
    value: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SelectionContainer {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
