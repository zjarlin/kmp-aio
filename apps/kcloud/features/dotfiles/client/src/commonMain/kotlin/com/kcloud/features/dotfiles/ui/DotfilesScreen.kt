package com.kcloud.features.dotfiles.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kcloud.features.dotfiles.DotfilesService
import com.kcloud.features.dotfiles.DotfilesSettings
import org.koin.compose.koinInject

@Composable
fun DotfilesScreen(
    service: DotfilesService = koinInject(),
) {
    var settings by remember { mutableStateOf(DotfilesSettings()) }
    var output by remember { mutableStateOf("等待执行 chezmoi 命令") }

    LaunchedEffect(Unit) {
        settings = service.loadSettings()
        val status = service.readStatus()
        output = buildString {
            appendLine("CLI 可用：${status.cliAvailable}")
            appendLine(status.versionOutput)
            appendLine(status.statusOutput)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Chezmoi Dotfiles", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = settings.repoUrl,
            onValueChange = { settings = settings.copy(repoUrl = it) },
            label = { Text("仓库地址") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = settings.workingDirectory,
            onValueChange = { settings = settings.copy(workingDirectory = it) },
            label = { Text("工作目录") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = settings.sourceDirectory,
            onValueChange = { settings = settings.copy(sourceDirectory = it) },
            label = { Text("源目录（可选）") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    settings = service.saveSettings(settings)
                    output = "配置已保存"
                },
            ) {
                Text("保存配置")
            }
            Button(
                onClick = {
                    output = service.initializeRepository().output
                },
            ) {
                Text("初始化")
            }
            Button(
                onClick = {
                    output = service.diff().output
                },
            ) {
                Text("查看 Diff")
            }
            Button(
                onClick = {
                    output = service.applyChanges().output
                },
            ) {
                Text("应用变更")
            }
        }

        Text(
            text = output,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
