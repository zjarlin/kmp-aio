package com.kcloud.plugins.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcloud.model.Theme
import com.kcloud.model.UpdateChannel
import com.kcloud.plugins.settings.SettingsEditorService
import com.kcloud.plugins.settings.SettingsSectionRegistry
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    settingsService: SettingsEditorService = koinInject(),
    settingsSectionRegistry: SettingsSectionRegistry = koinInject()
) {
    val persisted by settingsService.settings.collectAsState()
    var draft by remember(persisted) { mutableStateOf(persisted) }
    var status by remember { mutableStateOf("这里保存的是壳层通用设置，保存后会立刻写入本地 settings.json。") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.headlineSmall)
        Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SettingsSection("外观") {
            Text("主题模式", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Theme.entries.forEach { theme ->
                    Button(
                        onClick = { draft = draft.copy(theme = theme) }
                    ) {
                        Text(if (draft.theme == theme) "${theme.displayName()} ✓" else theme.displayName())
                    }
                }
            }

            OutlinedTextField(
                value = draft.language,
                onValueChange = { draft = draft.copy(language = it) },
                label = { Text("语言") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        SettingsSection("传输") {
            LabeledCheckbox(
                checked = draft.showNotifications,
                label = "显示通知",
                onCheckedChange = { checked ->
                    draft = draft.copy(showNotifications = checked)
                }
            )
            LabeledCheckbox(
                checked = draft.autoStart,
                label = "开机自动启动",
                onCheckedChange = { checked ->
                    draft = draft.copy(autoStart = checked)
                }
            )
            LabeledCheckbox(
                checked = draft.localStrategy.deleteAfterTransfer,
                label = "传输后删除本地源文件",
                onCheckedChange = { checked ->
                    draft = draft.copy(
                        localStrategy = draft.localStrategy.copy(deleteAfterTransfer = checked)
                    )
                }
            )
            LabeledCheckbox(
                checked = draft.localStrategy.createShortcut,
                label = "传输后创建快捷方式",
                onCheckedChange = { checked ->
                    draft = draft.copy(
                        localStrategy = draft.localStrategy.copy(createShortcut = checked)
                    )
                }
            )

            OutlinedTextField(
                value = draft.maxConcurrentTransfers.toString(),
                onValueChange = { input ->
                    draft = draft.copy(maxConcurrentTransfers = input.toIntOrNull() ?: draft.maxConcurrentTransfers)
                },
                label = { Text("最大并发传输数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        SettingsSection("更新") {
            Text("更新通道", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UpdateChannel.entries.forEach { channel ->
                    Button(
                        onClick = {
                            draft = draft.copy(
                                updateSettings = draft.updateSettings.copy(updateChannel = channel)
                            )
                        }
                    ) {
                        Text(
                            if (draft.updateSettings.updateChannel == channel) {
                                "${channel.displayName()} ✓"
                            } else {
                                channel.displayName()
                            }
                        )
                    }
                }
            }

            LabeledCheckbox(
                checked = draft.updateSettings.checkUpdatesAutomatically,
                label = "自动检查更新",
                onCheckedChange = { checked ->
                    draft = draft.copy(
                        updateSettings = draft.updateSettings.copy(checkUpdatesAutomatically = checked)
                    )
                }
            )
            LabeledCheckbox(
                checked = draft.updateSettings.downloadUpdatesAutomatically,
                label = "自动下载更新",
                onCheckedChange = { checked ->
                    draft = draft.copy(
                        updateSettings = draft.updateSettings.copy(downloadUpdatesAutomatically = checked)
                    )
                }
            )
            LabeledCheckbox(
                checked = draft.updateSettings.installUpdatesAutomatically,
                label = "自动安装更新",
                onCheckedChange = { checked ->
                    draft = draft.copy(
                        updateSettings = draft.updateSettings.copy(installUpdatesAutomatically = checked)
                    )
                }
            )
        }

        settingsSectionRegistry.contributors.forEach { contributor ->
            key(contributor.sectionId) {
                contributor.Section(
                    persisted = persisted,
                    draft = draft,
                    onDraftChange = { draft = it }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    settingsService.updateSettings(draft)
                    status = "设置已保存"
                }
            ) {
                Text("保存设置")
            }
            Button(
                onClick = {
                    draft = persisted
                    status = "已恢复到上次保存的设置"
                }
            ) {
                Text("放弃改动")
            }
        }
    }
}

private fun Theme.displayName(): String {
    return when (this) {
        Theme.LIGHT -> "浅色"
        Theme.DARK -> "深色"
        Theme.SYSTEM -> "跟随系统"
    }
}

private fun UpdateChannel.displayName(): String {
    return when (this) {
        UpdateChannel.STABLE -> "稳定"
        UpdateChannel.BETA -> "测试"
        UpdateChannel.DEV -> "开发"
    }
}
