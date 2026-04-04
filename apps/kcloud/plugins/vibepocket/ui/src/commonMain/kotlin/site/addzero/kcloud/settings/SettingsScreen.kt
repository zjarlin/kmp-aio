package site.addzero.kcloud.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import site.addzero.cupertino.workbench.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.cupertino.workbench.button.WorkbenchButton as Button
import site.addzero.cupertino.workbench.button.WorkbenchOutlinedButton as OutlinedButton
import site.addzero.kcloud.vibepocket.model.ConfigRuntimeInfo
import site.addzero.kcloud.screens.settings.SettingsViewModel
import site.addzero.kcloud.ui.StudioEmptyState
import site.addzero.kcloud.ui.StudioPill
import site.addzero.kcloud.ui.StudioSectionCard
import site.addzero.kcloud.ui.StudioTone
import site.addzero.kcloud.ui.SunoTokenApplyHint

@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val state = viewModel.state
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (!state.loaded) {
            StudioEmptyState(
                icon = "⏳",
                title = "读取配置中",
                description = "正在从本地 server 拉取你的音乐模块配置。",
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            MusicConfigEditor()
            LocalStorageCard(
                runtimeInfo = state.runtimeInfo,
                onOpenCacheDir = viewModel::openCacheDir,
            )
        }
    }
}

@Composable
private fun MusicConfigEditor() {
    val viewModel: SettingsViewModel = koinViewModel()
    val state = viewModel.state
    StudioSectionCard(
        modifier = Modifier.fillMaxWidth(),
        title = "音乐接口配置",
        subtitle = "设置页现在直接读写本地 server 的配置表，并显示实际保存结果。",
        action = {
            StudioPill(
                text = "Music service",
                tone = StudioTone.Tertiary,
            )
        },
    ) {
        SunoTokenApplyHint(
            intro = "如果你还没申请过 Suno API Token，这里可以直接跳去控制台申请。",
        )

        OutlinedTextField(
            value = state.runtimeConfig.apiToken,
            onValueChange = viewModel::updateSunoToken,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Suno API Token") },
            placeholder = { Text("sk-...") },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.runtimeConfig.baseUrl,
            onValueChange = viewModel::updateSunoBaseUrl,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Suno API Base URL") },
            placeholder = { Text("https://api.sunoapi.org/api/v1") },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.runtimeConfig.callbackUrl,
            onValueChange = viewModel::updateSunoCallbackUrl,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Suno Callback URL") },
            placeholder = { Text("https://xxxx.trycloudflare.com/api/suno/callback/default") },
            singleLine = true,
        )
        Text(
            text = "这里现在是可选项。大多数任务不填也能直接提交并轮询；如果你想在提交响应丢失时自动恢复 taskId，再补一个公网可访问的 HTTPS 回调地址即可。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        state.feedback.message?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.feedback.isError) {
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
                onClick = viewModel::saveConfig,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f),
            ) {
                if (state.isSaving) {
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
                onClick = viewModel::reloadWithFeedback,
                enabled = !state.isSaving,
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
                tone = StudioTone.Primary,
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
